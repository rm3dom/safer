@file:MustUseReturnValue
package com.swiftleap.safer.plugin.checkers

import com.swiftleap.safer.plugin.FunctionAndDescription
import com.swiftleap.safer.plugin.PluginConfiguration
import com.swiftleap.safer.plugin.TestEvent
import com.swiftleap.safer.plugin.TestHooks
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.error1
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
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
import kotlin.getValue


object UnsafeDiagnostics : KtDiagnosticsContainer() {
    val UNSAFE_FUNCTION_WARNING by warning1<PsiElement, String>()
    val UNSAFE_FUNCTION_ERROR by error1<PsiElement, String>()
    const val UNSAFE_FUNCTION_MESSAGE = "Unsafe function {0}, {1}"
    const val UNSAFE_DEFAULT_MESSAGE = "use a safer alternative instead"
    override fun getRendererFactory(): BaseDiagnosticRendererFactory = UnsafeErrorMessages
}

object UnsafeErrorMessages : BaseDiagnosticRendererFactory() {
    override val MAP by KtDiagnosticFactoryToRendererMap("UnsafeErrors") { map ->
        val _ = map.put(UnsafeDiagnostics.UNSAFE_FUNCTION_WARNING, "{0}", null)
        val _ = map.put(UnsafeDiagnostics.UNSAFE_FUNCTION_ERROR, "{0}", null)
    }
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
            UnsafeDiagnostics.UNSAFE_FUNCTION_MESSAGE,
            FirDiagnosticRenderers.CALLEE_NAME.render(statement),
            signature.message ?: UnsafeDiagnostics.UNSAFE_DEFAULT_MESSAGE
        )

    reportOn(
        statement.source,
        if (PluginConfiguration.unsafeWarnAsError)
            UnsafeDiagnostics.UNSAFE_FUNCTION_ERROR
        else
            UnsafeDiagnostics.UNSAFE_FUNCTION_WARNING,
        message,
        context
    )
}