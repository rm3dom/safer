package com.swiftleap.safer.plugin.checkers

import com.swiftleap.safer.plugin.*
import org.jetbrains.annotations.Contract
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.ExpressionCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirBlockChecker
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.declarations.FirAnonymousFunction
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.expressions.impl.FirSingleExpressionBlock
import org.jetbrains.kotlin.fir.references.toResolvedFunctionSymbol
import org.jetbrains.kotlin.fir.resolve.fqName
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.types.*

/**
 * Checker that ensures return values of certain functions are used.
 *
 * This checker analyzes expressions to determine if the return values of functions
 * that should have their results used are actually being used. If a return value
 * is not being used when it should be, it reports a diagnostic warning or error.
 */
internal class UnusedChecker(session: FirSession) : FirAdditionalCheckersExtension(session) {
    /**
     * Lazy-loaded list of signatures to check for unused return values.
     * Loaded from the plugin configuration.
     */
    private val signatures by lazy { PluginConfiguration.loadUnusedSignatures() }

    /**
     * Lazy-loaded map of function signatures grouped by function name.
     * Used to efficiently check if a function's return value should be used.
     */
    private val checkFunctions by lazy { signatures.filterIsInstance<Signature.Function>().groupBy { it.functionName } }

    /**
     * Lazy-loaded list of class signatures to check.
     * Used to determine if a type's return value should be used.
     */
    private val checkTypes by lazy { signatures.filterIsInstance<Signature.Clazz>() }

    /**
     * Lazy-loaded list of annotation signatures to check.
     * Used to determine if a function or type with certain annotations should have its return value used.
     */
    private val checkAnnotations by lazy { signatures.filterIsInstance<Signature.Annotation>() }


    /**
     * Checks if any annotation in the collection matches the configured annotation signatures.
     *
     * @return true if any annotation matches, false otherwise
     */
    @Contract(pure = true)
    private fun Iterable<FirAnnotation>.isChecked() =
        firstOrNull { annot ->
            annot.fqName(session)?.let { fqName ->
                //TODO do not map the annotations when we do not need to
                val args = annot.argumentMapping.mapping
                    .map { it.key.toString() to (it.value as? FirLiteralExpression<*>)?.value?.toString() }
                    .toMap()
                checkAnnotations.anyMatch(fqName, args)
            } == true
        } != null

    /**
     * Checks if a function symbol is marked for return value checking.
     * A function is marked if it has a matching annotation or if its signature
     * matches one of the configured function signatures.
     *
     * @return true if the function should have its return value checked, false otherwise
     */
    @Contract(pure = true)
    private fun FirFunctionSymbol<*>.isChecked() =
        annotations.isChecked() ||
                checkFunctions
                    .getOrDefault(name.toString(), emptyList())
                    .firstOrNull { it.matches(this) } != null

    /**
     * Block checker that identifies statements with unused return values.
     * This checker analyzes blocks of code to find expressions whose return values
     * should be used but are being ignored.
     */
    private val blockChecker = object : FirBlockChecker(MppCheckerKind.Common) {
        /**
         * Checks a block of code for statements with unused return values.
         *
         * This method examines each statement in the block and determines if its
         * return value should be used based on the configured signatures and annotations.
         * It skips certain types of expressions and contexts where the return value
         * is implicitly used.
         *
         * @param expression The block expression to check
         * @param context The checker context
         * @param reporter The diagnostic reporter to report issues
         */
        override fun check(
            expression: FirBlock,
            context: CheckerContext,
            reporter: DiagnosticReporter
        ) {
            // lambda's `() -> myFn()` or when branch `-> myFn()`
            if (expression is FirSingleExpressionBlock) return

            expression.statements.forEachIndexed { index, statement ->

                if (statement !is FirFunctionCall
                    && statement !is FirWhenExpression
                    && statement !is FirTryExpression
                    && statement !is FirElvisExpression
                ) return@forEachIndexed

                val type = statement.resolvedType
                if (type.isUnit || type.isNothing) return@forEachIndexed

                val lastStatement = expression.statements.size - 1 == index

                // If this is the last statement in a lambda or anonymous function,
                // its return value is being used as the lambda's return value
                if (lastStatement && context.containingElements.any {
                        it is FirAnonymousFunction || it is FirReturnExpression || it is FirProperty
                    }
                ) return@forEachIndexed

                val classSymbol = type.toRegularClassSymbol(session)
                val classSymbolAnnotations = classSymbol?.annotations
                // type checks
                val isAdditionalCheckType = type.classId?.let { checkTypes.anyMatch(it) } == true
                val fnSymbol = statement.calleeReference.toResolvedFunctionSymbol()
                // annotated function checks
                val fnCheckReturn = fnSymbol?.isChecked() == true
                // annotated type checks?
                val typeCheckReturn = classSymbolAnnotations?.isChecked() == true

                if (typeCheckReturn || fnCheckReturn || isAdditionalCheckType)
                    reporter.reportUnused(context, statement, type)
            }
        }
    }

    /**
     * Provides the set of expression checkers used by this extension.
     * In this case, it only includes the block checker for unused return values.
     */
    override val expressionCheckers: ExpressionCheckers = object : ExpressionCheckers() {
        /**
         * The set of block checkers to be applied during analysis.
         * Contains only the unused return value block checker.
         */
        override val blockCheckers: Set<FirBlockChecker> = setOf(blockChecker)
    }
}
