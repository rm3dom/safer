@file:MustUseReturnValue
package com.swiftleap.safer.plugin.checkers

import com.swiftleap.safer.plugin.PluginConfiguration
import com.swiftleap.safer.plugin.matches
import org.jetbrains.kotlin.DeprecatedForRemovalCompilerApi
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.ExpressionCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirFunctionCallChecker
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.references.toResolvedFunctionSymbol
import org.jetbrains.kotlin.fir.types.isNothing
import org.jetbrains.kotlin.fir.types.isUnit
import org.jetbrains.kotlin.fir.types.resolvedType

/**
 * Checker that identifies and reports usage of unsafe functions.
 *
 * This checker analyzes function calls to determine if they match any of the
 * configured unsafe function signatures. If a match is found, it reports
 * a diagnostic warning or error.
 */
internal class UnsafeChecker(session: FirSession) : FirAdditionalCheckersExtension(session) {
    /**
     * Lazy-loaded list of unsafe function signatures to check against.
     * Loaded from the plugin configuration.
     */
    private val signatures by lazy {
        PluginConfiguration.loadUnsafeSignatures()
    }

    /**
     * Function call checker that identifies unsafe function calls.
     * This checker is applied to all function calls in the code being analyzed.
     */
    private val callChecker = object : FirFunctionCallChecker(MppCheckerKind.Common) {
        context(context: CheckerContext, reporter: DiagnosticReporter)
        override fun check(expression: FirFunctionCall) {
            val type = expression.resolvedType
            if (type.isUnit || type.isNothing)
                return
            val fnSymbol = expression.calleeReference.toResolvedFunctionSymbol()
            if (fnSymbol == null)
                return
            val match = signatures
                .getOrDefault(fnSymbol.name.toString(), emptyList())
                .firstOrNull { it.signature.matches(fnSymbol) }
            if (match == null)
                return

            reporter.reportUnsafe(context, match, expression, fnSymbol)
        }
    }

    /**
     * Provides the set of expression checkers used by this extension.
     * In this case, it only includes the function call checker for unsafe functions.
     */
    override val expressionCheckers: ExpressionCheckers = object : ExpressionCheckers() {
        /**
         * The set of function call checkers to be applied during analysis.
         * Contains only the unsafe function call checker.
         */
        override val functionCallCheckers: Set<FirFunctionCallChecker> = setOf(callChecker)
    }
}
