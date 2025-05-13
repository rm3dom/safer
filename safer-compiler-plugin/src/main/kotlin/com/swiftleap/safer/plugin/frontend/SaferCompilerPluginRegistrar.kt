package com.swiftleap.safer.plugin.frontend

import com.swiftleap.safer.plugin.*
import com.swiftleap.safer.plugin.backend.UnsafeChecker
import com.swiftleap.safer.plugin.backend.UnusedChecker
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

/**
 * Registrar for the Safer compiler plugin.
 *
 * This class is responsible for registering the plugin's extensions with the Kotlin compiler.
 * It checks if the plugin is enabled and if the Kotlin version is compatible before registering
 * the appropriate FIR extensions.
 */
@OptIn(ExperimentalCompilerApi::class)
internal class SaferCompilerPluginRegistrar : CompilerPluginRegistrar() {

    /**
     * Indicates that this plugin supports the K2 compiler.
     * The Safer plugin is designed to work with the K2 compiler only.
     */
    override val supportsK2: Boolean = true

    /**
     * Registers the plugin's extensions with the Kotlin compiler.
     *
     * This method is called by the Kotlin compiler during the compilation process.
     * It checks if the plugin is enabled and if the Kotlin version is compatible,
     * then registers the appropriate FIR extensions based on the plugin configuration.
     *
     * @param configuration The compiler configuration
     * @throws SaferException If the Kotlin version is incompatible with the plugin
     */
    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        System.err.println("Registering Safer compiler plugin")

        if (!PluginConfiguration.unusedEnabled
            && !PluginConfiguration.unsafeEnabled
        )
            return

        if (KotlinVersion.CURRENT.toString() != BuildInfo.kotlinVersion) {
            val subVersion = BuildInfo.projectVersion.split("-", limit = 2).getOrNull(1)
            throw SaferException(
                """
                Safer compiler plugin requires Kotlin version ${BuildInfo.kotlinVersion} but ${KotlinVersion.CURRENT} was found.
                Update your build script to use the correct version of Safer.
                plugins {
                    id("com.swiftleap.safer") version "${KotlinVersion.CURRENT}-${subVersion}"
                }
                """.trimIndent()
            )
        }

        if (PluginConfiguration.unusedEnabled)
            FirExtensionRegistrarAdapter.registerExtension(CheckReturnFirExtensionRegistrar)

        if (PluginConfiguration.unsafeEnabled)
            FirExtensionRegistrarAdapter.registerExtension(UnsafeFirExtensionRegistrar)

        TestHooks.trigger(TestEvent.PluginLoaded)
    }
}

/**
 * FIR extension registrar for the unused return value checking functionality.
 *
 * This registrar adds the UnusedChecker to the Kotlin compiler's FIR pipeline
 * to check for unused return values.
 */
private object CheckReturnFirExtensionRegistrar : FirExtensionRegistrar() {
    /**
     * Configures the plugin by adding the UnusedChecker extension.
     */
    override fun ExtensionRegistrarContext.configurePlugin() {
        +::UnusedChecker
    }
}

/**
 * FIR extension registrar for the unsafe function checking functionality.
 *
 * This registrar adds the UnsafeChecker to the Kotlin compiler's FIR pipeline
 * to check for usage of unsafe functions.
 */
private object UnsafeFirExtensionRegistrar : FirExtensionRegistrar() {
    /**
     * Configures the plugin by adding the UnsafeChecker extension.
     */
    override fun ExtensionRegistrarContext.configurePlugin() {
        +::UnsafeChecker
    }
}
