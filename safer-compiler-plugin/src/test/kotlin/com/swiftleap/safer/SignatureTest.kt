package com.swiftleap.safer

import com.swiftleap.safer.plugin.Signature
import kotlin.test.Test
import kotlin.test.assertEquals

class SignatureTest {
    @Test
    fun testParseSignature() {
        val kotlinPackage = Signature.Package("kotlin")

        val signatures = arrayOf(
            //Invalid cases
            "     " to null,
            "kotlin.*.get()" to null,
            "kotlin.*" to null,

            //Valid cases
            "org.jetbrains/annotations.@Contract(pure=true)" to Signature.Annotation(
                Signature.Package("org.jetbrains.annotations"),
                "Contract",
                mapOf("pure" to "true")
            ),
            "@Annotation" to Signature.Annotation(kotlinPackage, "Annotation"),
            "*.@Annotation()" to Signature.Annotation(Signature.AnyPackage, "Annotation"),
            "*.`class name`" to Signature.Clazz(Signature.AnyPackage, "class name"),
            "*/Array" to Signature.Clazz(Signature.AnyPackage, "Array"),
            ".Array" to Signature.Clazz(kotlinPackage, "Array"),
            "*" to Signature.AnyClass,
            "Array" to Signature.Clazz(kotlinPackage, "Array"),
            "Int" to Signature.Clazz(kotlinPackage, "Int"),

            "kotlin.collections.first()" to Signature.Function(
                Signature.Package("kotlin.collections"), null, "first",
                emptyList()
            ),

            "Array.`function name`(*)" to Signature.Function(
                kotlinPackage, "Array", "function name",
                listOf(Signature.AnyClass)
            ),

            "Array/set(Int, *)" to Signature.Function(
                kotlinPackage, "Array", "set",
                listOf(Signature.Clazz(kotlinPackage, "Int"), Signature.AnyClass)
            ),

            "Array.set(*, *)" to Signature.Function(
                kotlinPackage, "Array", "set",
                listOf(Signature.AnyClass, Signature.AnyClass)
            ),

            "Array.get(Int)" to Signature.Function(
                kotlinPackage, "Array", "get",
                listOf(Signature.Clazz(kotlinPackage, "Int"))
            ),
        )

        signatures.forEach { (signature, expected) ->
            assertEquals(expected, Signature.parse(signature), "Failed to parse signature: $signature")
        }
    }
}