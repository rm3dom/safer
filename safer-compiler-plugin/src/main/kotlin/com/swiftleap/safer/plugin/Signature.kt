@file:MustUseReturnValue
package com.swiftleap.safer.plugin

import org.jetbrains.annotations.Contract
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

/**
 * Represents a signature with an optional description.
 * Used for storing signatures with its diagnostics message.
 *
 * @property signature The signature
 * @property message Optional diagnostic message to report
 */
data class SignatureAndMessage(val signature: Signature, val message: String?)

/**
 * Represents a function signature with an optional description.
 * Used for storing signatures with its diagnostics message.
 *
 * @property signature The function signature
 * @property message Optional diagnostic message to report
 */
data class FunctionAndDescription(val signature: Signature.Function, val message: String?)

/**
 * Represents signatures for classes, methods, and annotations.
 * Used for matching and identifying elements in code.
 *
 * This interface hierarchy provides a type-safe way to represent and match
 * different kinds of signatures in the codebase.
 */
//A bit ugly, we will test it to death.
sealed interface Signature {
    /**
     * Represents a package specification.
     * Can be either a specific package or any package.
     */
    sealed interface PackageSpec

    /**
     * Represents any package (wildcard).
     * Used when the package doesn't matter for matching.
     */
    data object AnyPackage : PackageSpec, Signature

    /**
     * Represents a specific package.
     *
     * @property packageName The fully qualified package name
     */
    data class Package(val packageName: String) : PackageSpec, Signature

    /**
     * Represents a class specification.
     * Can be either a specific class or any class.
     */
    sealed interface ClassSpec : Signature

    /**
     * Common interface for classes and annotations.
     * Provides access to the fully qualified name, class name, and package.
     */
    sealed interface ClassOrAnnotation : Signature {
        /**
         * The fully qualified name of the class or annotation, or null if it's in any package.
         */
        val fqName: String?

        /**
         * The simple name of the class or annotation.
         */
        val className: String

        /**
         * The package specification for this class or annotation.
         */
        val packageSpec: PackageSpec
    }

    /**
     * Represents any class (wildcard).
     * Used when the specific class doesn't matter for matching.
     */
    data object AnyClass : ClassSpec, Signature

    /**
     * Represents a specific class.
     *
     * @property packageSpec The package specification for this class
     * @property className The simple name of the class
     */
    data class Clazz(
        override val packageSpec: PackageSpec,
        override val className: String
    ) : ClassSpec, ClassOrAnnotation, Signature {
        /**
         * The fully qualified name of the class, or null if it's in any package.
         */
        override val fqName = when (packageSpec) {
            AnyPackage -> null
            is Package -> "${packageSpec.packageName}.$className"
        }
    }

    /**
     * Represents an annotation with optional arguments.
     *
     * @property packageSpec The package specification for this annotation
     * @property className The simple name of the annotation
     * @property arguments Map of argument names to their values
     */
    data class Annotation(
        override val packageSpec: PackageSpec,
        override val className: String,
        val arguments: Map<String, String?> = emptyMap()
    ) : ClassOrAnnotation, Signature {
        /**
         * The fully qualified name of the annotation, or null if it's in any package.
         */
        override val fqName = when (packageSpec) {
            AnyPackage -> null
            is Package -> "${packageSpec.packageName}.$className"
        }
    }

    /**
     * Represents a function signature.
     *
     * @property packageSpec The package specification for this function
     * @property className The simple name of the containing class, or null for top-level functions
     * @property functionName The name of the function
     * @property parameters List of parameter type specifications
     */
    data class Function(
        val packageSpec: PackageSpec,
        val className: String?,
        val functionName: String,
        val parameters: List<ClassSpec>
    ) : Signature {
        /**
         * The fully qualified name of the function, or null if it's in any package.
         */
        val fqName = when (packageSpec) {
            AnyPackage -> null
            is Package -> if (className == null)
                "${packageSpec.packageName}.$className.$functionName"
            else
                "${packageSpec.packageName}.$functionName"
        }
    }

    /**
     * Contains factory methods for parsing signature strings.
     */
    companion object {
        /**
         * Parses a function signature string into a function name and parameter types.
         *
         * @param functionSignature The function signature string in the format "functionName(param1, param2, ...)"
         * @return A pair of the function name and a list of parameter type specifications, or null if parsing fails
         */
        @Contract(pure = true)
        private fun parseFunction(functionSignature: String): Pair<String, List<ClassSpec>>? {
            val parts = functionSignature.split('(', ')', ',').map { it.trim() }.filter { it.isNotBlank() }
            val name = parts.firstOrNull() ?: return null
            val params = parts.drop(1).map {
                parse(it) as? ClassSpec ?: return null
            }
            return name.trim('`') to params
        }

        /**
         * Parses an annotation signature string into an annotation name and arguments.
         *
         * @param annotationSignature The annotation signature string in the format "@AnnotationName(key1=value1, key2=value2, ...)"
         * @return A pair of the annotation name and a map of argument names to values, or null if parsing fails
         */
        @Contract(pure = true)
        private fun parseAnnotation(annotationSignature: String): Pair<String, Map<String, String>>? {
            val parts = annotationSignature.split('(', ')', ',').map { it.trim() }.filter { it.isNotBlank() }
            val name = parts.firstOrNull()?.trimStart('@') ?: return null
            val params = parts.drop(1).mapNotNull {
                val properties = it.split('=', ':').map { p -> p.trim() }.filter { p -> p.isNotBlank() }
                val key = properties.getOrNull(0)
                val value = properties.getOrNull(1)
                if (key == null || value == null)
                    null
                else
                    key to value
            }.toMap()
            return name to params
        }

        /**
         * Parses a signature string into a Signature object.
         * Supports various formats including class, annotation, and function signatures.
         *
         * @param signature The signature string to parse
         * @return The parsed Signature object, or null if parsing fails
         */
        @Contract(pure = true)
        fun parse(signature: String): Signature? {
            val parts = signature.split('.', '/').map { it.trim() }.filter { it.isNotBlank() }
            if (parts.size == 1 && parts.firstOrNull() == "*") return AnyClass
            var packageName = ""
            var className: String? = null
            var annotation: Pair<String, Map<String, String>>? = null
            var function: Pair<String, List<ClassSpec>>? = null
            for (part in parts) {
                when {
                    //Annotation may contain '(', its first
                    part.firstOrNull() == '@' -> annotation = parseAnnotation(part)
                    part.contains('(') -> function = parseFunction(part)
                    part.firstOrNull()?.isUpperCase() == true -> className = part
                    part.firstOrNull() == '`' -> className = part.trim('`')
                    else -> packageName += ".$part"
                }
            }

            val packageSpec = when (val p = packageName.trim('.')) {
                "*" -> AnyPackage
                "" -> Package("kotlin")
                else -> if (packageName.contains('*'))
                //Can't have both any package and any class
                    return null
                else
                    Package(p)
            }

            if (annotation != null)
                return Annotation(packageSpec, annotation.first, annotation.second)
            if (function != null)
                return Function(packageSpec, className, function.first, function.second)
            if (className != null)
                return Clazz(packageSpec, className.trimStart('@'))
            return null
        }
    }
}

/**
 * Checks if this signature matches the given class ID.
 *
 * @param classId The class ID to match against
 * @return true if this signature matches the class ID, false otherwise
 */
@Contract(pure = true)
fun Signature.matches(classId: ClassId): Boolean =
    when (val c = this) {
        Signature.AnyClass -> true
        Signature.AnyPackage -> false
        is Signature.ClassOrAnnotation -> when (val p = packageSpec) {
            Signature.AnyPackage -> c.className == classId.shortClassName.asString()
            is Signature.Package -> c.className == classId.shortClassName.asString() && p.packageName == classId.packageFqName.asString()
        }

        is Signature.Function -> false
        is Signature.Package -> false
    }

@Contract(pure = true)
fun Signature.matches(type: ConeKotlinType): Boolean =
    when (val c = this) {
        Signature.AnyClass -> true
        Signature.AnyPackage -> false
        is Signature.ClassOrAnnotation -> {
            val classId = type.classId ?: return false
            when (val p = packageSpec) {
                Signature.AnyPackage -> c.className == classId.shortClassName.asString()
                is Signature.Package -> c.className == classId.shortClassName.asString() && p.packageName == classId.packageFqName.asString()
            }
        }
        is Signature.Function -> false
        is Signature.Package -> false
    }

/**
 * Checks if any signature in this collection matches the given class ID.
 *
 * @param classId The class ID to match against
 * @return true if any signature matches the class ID, false otherwise
 */
@Contract(pure = true)
fun Iterable<Signature>.anyMatch(classId: ClassId): Boolean =
    any { it.matches(classId) }

/**
 * Checks if this signature matches the given fully qualified name.
 *
 * @param fqName The fully qualified name to match against
 * @return true if this signature matches the fully qualified name, false otherwise
 */
@Contract(pure = true)
fun Signature.matches(fqName: FqName): Boolean =
    when (val c = this) {
        Signature.AnyClass -> true
        Signature.AnyPackage -> false
        is Signature.Clazz -> when (packageSpec) {
            Signature.AnyPackage -> {
                fqName.toString().endsWith(c.className)
            }

            is Signature.Package -> fqName.toString() == c.fqName
        }

        is Signature.Function -> when (packageSpec) {
            Signature.AnyPackage -> fqName.toString().endsWith(c.functionName)
            is Signature.Package -> fqName.toString() == c.fqName
        }

        is Signature.Package -> false
        is Signature.Annotation -> when (packageSpec) {
            Signature.AnyPackage -> {
                fqName.toString().endsWith(c.className)
            }

            is Signature.Package -> fqName.toString() == c.fqName
        }
    }

/**
 * Checks if this map contains all the key-value pairs from another map.
 * Performs case-insensitive comparison for string values.
 *
 * @param other The map of key-value pairs to check for
 * @return true if this map contains all the key-value pairs, false otherwise
 */
private fun Map<String, String?>.containsAll(other: Map<String, String?>): Boolean =
    other.all { (k, v) -> this[k] == v || this[k]?.equals(v, true) == true }

/**
 * Checks if any annotation signature in this collection matches the given fully qualified name and arguments.
 *
 * @param fqName The fully qualified name to match against
 * @param args The map of argument names to values to check for
 * @return true if any annotation matches both the name and arguments, false otherwise
 */
@Contract(pure = true)
fun Iterable<Signature.Annotation>.anyMatch(fqName: FqName, args: Map<String, String?>): Boolean =
    any { it.matches(fqName) && args.containsAll(it.arguments) }

/**
 * Checks if any signature in this collection matches the given fully qualified name.
 *
 * @param fqName The fully qualified name to match against
 * @return true if any signature matches the name, false otherwise
 */
@Contract(pure = true)
fun Iterable<Signature>.anyMatch(fqName: FqName): Boolean =
    any { it.matches(fqName) }


@Contract(pure = true)
        /**
         * Checks if this function signature matches the given function symbol.
         * Performs detailed matching of package, class, function name, and parameter types.
         *
         * @param fn The function symbol to match against
         * @return true if this function signature matches the function symbol, false otherwise
         */
fun Signature.Function.matches(fn: FirFunctionSymbol<*>): Boolean {
    val fnSig = this
    val symbolMatches = when (packageSpec) {
        Signature.AnyPackage -> {
            val callId = fn.callableId
            val className = callId.className?.shortName()?.asString()
            val callName = callId.callableName.asString()
            if (fnSig.className != null)
                className == fnSig.className && callName == fnSig.functionName
            else
                callName == fnSig.functionName
        }

        is Signature.Package -> {
            val callId = fn.callableId
            val pkg = callId.packageName.asString()
            val clsName = callId.className?.shortName()?.asString()
            val callName = callId.callableName.asString()
            if (fnSig.className != null)
                packageSpec.packageName == pkg
                        && clsName == fnSig.className
                        && callName == fnSig.functionName
            else
                packageSpec.packageName == pkg
                        && callName == fnSig.functionName
        }
    }
    if (!symbolMatches) return false

    val paramTypes = fn.valueParameterSymbols.map { it.resolvedReturnType }

    if (parameters.size != paramTypes.size) return false

    val paramsMatches = parameters.zip(paramTypes).all { (p, t) -> p.matches(t) }

    return paramsMatches
}


