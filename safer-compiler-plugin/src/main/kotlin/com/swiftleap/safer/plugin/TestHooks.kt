package com.swiftleap.safer.plugin

import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol

/**
 * Represents events that can occur during the plugin's operation.
 * Used for testing and debugging purposes.
 */
internal sealed class TestEvent {
    /**
     * Event triggered when the plugin is loaded.
     */
    object PluginLoaded : TestEvent()

    /**
     * Event triggered when a result is not used when it should be.
     *
     * @property element The FIR element with the unused result
     */
    class ResultNotUsed(val element: FirElement) : TestEvent()

    /**
     * Event triggered when an unsafe function is used.
     *
     * @property signature The signature of the unsafe function with its description
     * @property element The function symbol of the unsafe function
     */
    class UnsafeFunction(val signature: FunctionAndDescription, val element: FirFunctionSymbol<*>) : TestEvent()
}

/**
 * Provides a mechanism for registering and triggering test hooks.
 * Used for testing and debugging the plugin's behavior.
 */
internal object TestHooks {
    /**
     * List of registered hook functions that will be called when events are triggered.
     */
    private val hooks: MutableList<(TestEvent) -> Unit> = mutableListOf()

    /**
     * Triggers all registered hooks with the given event.
     *
     * @param event The event to trigger
     */
    fun trigger(event: TestEvent) = hooks.forEach { it(event) }

    /**
     * Registers a new hook function to be called when events are triggered.
     *
     * @param hook The hook function to register
     * @return true (as per the contract of MutableCollection.add)
     */
    fun register(hook: (TestEvent) -> Unit) = hooks.add(hook)
}
