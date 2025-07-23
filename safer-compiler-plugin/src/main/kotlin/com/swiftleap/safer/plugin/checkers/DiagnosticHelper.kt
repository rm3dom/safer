@file:MustUseReturnValue
package com.swiftleap.safer.plugin.checkers

import com.swiftleap.safer.plugin.FunctionAndDescription
import com.swiftleap.safer.plugin.PluginConfiguration
import com.swiftleap.safer.plugin.TestEvent
import com.swiftleap.safer.plugin.TestHooks
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.error1
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.diagnostics.warning1
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirDiagnosticRenderers
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.classId
import java.text.MessageFormat

private object Defaults {
    val WARNING by warning1<PsiElement, String>()
    val ERROR by error1<PsiElement, String>()
    const val UNUSED_RESULT_MESSAGE = "Unused result {0} -> {1}"
    const val UNSAFE_FUNCTION_MESSAGE = "Unsafe function {0}, {1}"
    const val UNSAFE_DEFAULT_MESSAGE = "use a safer alternative instead"
}

/**
 * Reports an unused return value diagnostic.
 *
 * Triggers `TestEvent.ResultNotUsed` test hook event.
 *
 * @param context The checker context
 * @param statement The expression with the unused return value
 * @param type The type of the unused return value
 */
internal fun DiagnosticReporter.reportUnused(
    context: CheckerContext,
    statement: FirExpression,
    type: ConeKotlinType
) {
    TestHooks.trigger(TestEvent.ResultNotUsed(statement))

    val message =
        MessageFormat.format(
            Defaults.UNUSED_RESULT_MESSAGE,
            FirDiagnosticRenderers.CALLEE_NAME.render(statement),
            type.classId?.shortClassName ?: type
        )

    reportOn(
        statement.source,
        if (PluginConfiguration.unusedWarnAsError)
            Defaults.ERROR
        else
            Defaults.WARNING,
        message,
        context
    )
}


/**
 * Reports a diagnostic for usage of an unsafe function.
 *
 * Triggers `TestEvent.UnsafeFunction` test hook event.
 *
 * @param context The checker context
 * @param signature The function signature and its associated alternative message
 * @param statement The function call expression being reported
 * @param symbol The symbol representing the called function
 */
internal fun DiagnosticReporter.reportUnsafe(
    context: CheckerContext,
    signature: FunctionAndDescription,
    statement: FirFunctionCall,
    symbol: FirFunctionSymbol<*>,
) {
    TestHooks.trigger(TestEvent.UnsafeFunction(signature, symbol))

    val message =
        MessageFormat.format(
            Defaults.UNSAFE_FUNCTION_MESSAGE,
            FirDiagnosticRenderers.CALLEE_NAME.render(statement),
            signature.message ?: Defaults.UNSAFE_DEFAULT_MESSAGE
        )

    reportOn(
        statement.source,
        if (PluginConfiguration.unsafeWarnAsError)
            Defaults.ERROR
        else
            Defaults.WARNING,
        message,
        context
    )
}