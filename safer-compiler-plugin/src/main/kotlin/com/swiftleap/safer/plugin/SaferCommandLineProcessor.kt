package com.swiftleap.safer.plugin

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOptionProcessingException
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

/**
 * Command line processor for the Safer compiler plugin.
 *
 * This class processes command line arguments to configure the plugin.
 * It handles all the plugin-specific options and applies them to the plugin configuration.
 */
@OptIn(ExperimentalCompilerApi::class)
internal class SaferCommandLineProcessor : CommandLineProcessor {

    /**
     * The unique identifier for this plugin.
     * Used by the Kotlin compiler to identify this plugin.
     */
    override val pluginId: String = BuildInfo.projectId

    /**
     * List of command line options supported by this plugin.
     * These options can be specified on the command line to configure the plugin's behavior.
     */
    override val pluginOptions = ConfigurationOption.pluginOptions

    /**
     * Processes a single command line option.
     *
     * This method is called by the Kotlin compiler for each plugin option encountered
     * on the command line. It finds the corresponding option in the plugin's options
     * list and applies its value to the plugin configuration.
     *
     * @param option The command line option to process
     * @param value The value provided for the option
     * @param configuration The compiler configuration
     * @throws CliOptionProcessingException If the option is unknown or the value is invalid
     */
    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration,
    ) {
        val opt = pluginOptions.firstOrNull { it.optionName == option.optionName }
            ?: throw CliOptionProcessingException("Unknown option: ${option.optionName}")
        try {
            opt.applyToCompilation(value)
        } catch (_: Exception) {
            throw CliOptionProcessingException(
                "Invalid value for option ${option.optionName} expected ${option.valueDescription} but was $value",
            )
        }
    }
}
