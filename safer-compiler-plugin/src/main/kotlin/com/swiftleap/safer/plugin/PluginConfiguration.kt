package com.swiftleap.safer.plugin

import com.swiftleap.safer.BuildInfo
import org.jetbrains.annotations.Contract
import org.jetbrains.kotlin.cli.common.toBooleanLenient
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import kotlin.reflect.KProperty0


/**
 * Global configuration for the plugin. This object stores the configuration of the plugin. It should only be mutated
 * during the command line processing.
 */
internal object PluginConfiguration {
    const val SEPERATOR = "&"

    /**
     * Controls whether the unused return value checking is enabled.
     * Default is true.
     */
    var unusedEnabled: Boolean = true

    /**
     * Controls whether unused return value warnings should be treated as errors.
     * Default is false.
     */
    var unusedWarnAsError: Boolean = false

    /**
     * Set of signature strings for functions or types that should have their return values used.
     * These are user-provided signatures.
     */
    var unusedSignatures: Set<String> = setOf()

    /**
     * Set of preset library names to load unused signatures from.
     * Default includes the "default" preset.
     */
    var unusedPresetLibs: Set<String> = setOf("default")

    /**
     * Loads and parses all unused signatures from both user-provided signatures and preset libraries.
     *
     * @return A list of parsed signatures to check for unused return values
     */
    @Contract(pure = true)
    fun loadUnusedSignatures(): List<Signature> =
        unusedSignatures
            .mapNotNull {
                try {
                    Signature.parse(it)
                } catch (_: Exception) {
                    //We should validate / error at configuration time
                    null
                }
            } + unusedPresetLibs
            .flatMap { libName ->
                loadLibrary("unused-$libName.txt").map { it.signature }
            }

    /**
     * Controls whether the unsafe function checking is enabled.
     * Default is true.
     */
    var unsafeEnabled: Boolean = true

    /**
     * Controls whether unsafe function warnings should be treated as errors.
     * Default is false.
     */
    var unsafeWarnAsError: Boolean = false

    /**
     * Set of signature strings for functions that are considered unsafe.
     * These are user-provided signatures.
     */
    var unsafeSignatures: Set<String> = setOf()

    /**
     * Set of preset library names to load unsafe function signatures from.
     */
    var unsafePresetLibs: Set<String> = setOf()

    /**
     * Loads and parses all unsafe function signatures from both user-provided signatures and preset libraries.
     *
     * @return A map of function names to lists of function signatures with descriptions
     */
    @Contract(pure = true)
    fun loadUnsafeSignatures(): Map<String, List<FunctionAndDescription>> {
        val lib = unsafePresetLibs.flatMap { libName ->
            loadLibrary("unsafe-$libName.txt")
        }
        val user = unsafeSignatures
            .mapNotNull {
                try {
                    Signature.parse(it)?.let { SignatureAndMessage(it, null) }
                } catch (_: Exception) {
                    //We should validate / error at configuration time
                    null
                }
            }
        return (lib + user)
            .mapNotNull {
                if (it.signature is Signature.Function)
                    FunctionAndDescription(it.signature, it.message)
                else
                    null
            }
            .groupBy { it.signature.functionName }
    }

    /**
     * Loads signature definitions from a resource file.
     *
     * @param name The name of the resource file to load
     * @return A list of signatures with their descriptions
     */
    @Contract(pure = true)
    private fun loadLibrary(name: String): List<SignatureAndMessage> {
        val stream = this::class.java.classLoader.getResourceAsStream(name) ?: return emptyList()
        stream.use { stream ->
            return stream.bufferedReader().useLines { lines ->
                lines.mapNotNull { line ->
                    val line = line.trim()
                    if (line.isBlank() || line.startsWith("#"))
                        null
                    else {
                        val parts = line.split("->").map { it.trim() }
                        val signature = Signature.parse(parts.getOrNull(0) ?: "")
                        signature?.let { SignatureAndMessage(it, parts.getOrNull(1)) }
                    }
                }.toList()
            }
        }
    }
}

/**
 * Converts a string to a set of configuration values.
 * Splits the string by commas, spaces, or colons and returns the resulting set.
 * If the resulting set is empty, returns the default set.
 *
 * @param default The default set to return if the resulting set is empty
 * @return A set of configuration values
 */
@Contract(pure = true)
private fun String.toConfigSet(default: Set<String>): Set<String> {
    val set = this.trim('\'', '"')
        .split(PluginConfiguration.SEPERATOR)
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .toSet()
    return set.ifEmpty { default }
}

/**
 * Converts a set of strings to a configuration string.
 * Joins the set elements with a comma and space separator.
 *
 * @return A string representation of the set for configuration purposes
 */
@Contract(pure = true)
private fun Set<String>.toConfigString(): String =
    joinToString(separator = PluginConfiguration.SEPERATOR)

/**
 * Creates a plugin CLI option string for a boolean property.
 *
 * @param value The value to use (defaults to the current property value)
 * @return A string in the format "plugin:projectId:propertyName=value"
 */
@Contract(pure = true)
internal fun KProperty0<Boolean>.asPluginCliOption(value: Boolean = get()): String =
    "plugin:${BuildInfo.projectId}:${name}=${value}"

/**
 * Creates a plugin CLI option string for a Set<String> property.
 *
 * @param value The value to use (defaults to the current property value)
 * @return A string in the format "plugin:projectId:propertyName=value1, value2, ..."
 */
@Contract(pure = true)
internal fun KProperty0<Set<String>>.asPluginCliOption(value: Set<String> = get()): String =
    "plugin:${BuildInfo.projectId}:${name}=\'${value.toConfigString()}\'"


/**
 * Represents a command-line configuration option for the plugin.
 * Implements AbstractCliOption to integrate with the Kotlin compiler plugin system.
 *
 * @property optionName The name of the option as it appears on the command line
 * @property valueDescription A description of the expected value format
 * @property description A human-readable description of the option
 * @property allowMultipleOccurrences Whether the option can be specified multiple times
 * @property apply A function that applies the option value to the plugin configuration
 */
internal class ConfigurationOption(
    override val optionName: String,
    override val valueDescription: String,
    override val description: String,
    override val allowMultipleOccurrences: Boolean = false,
    private val apply: (String) -> Unit,
) : AbstractCliOption {

    /**
     * Indicates whether the option is required.
     * All options in this plugin are optional.
     */
    override val required: Boolean
        get() = false

    /**
     * Applies the option value to the plugin configuration.
     *
     * @param value The string value of the option from the command line
     */
    fun applyToCompilation(value: String): Unit = apply(value)

    /**
     * Contains the list of all available configuration options for the plugin.
     */
    companion object {
        /**
         * The complete list of plugin configuration options.
         * These options can be specified on the command line to configure the plugin.
         */
        val pluginOptions: List<ConfigurationOption> = listOf(
            //Check return
            ConfigurationOption(
                PluginConfiguration::unusedEnabled.name,
                "<true|false>",
                "Whether the plugin is enabled or not",
            ) { PluginConfiguration.unusedEnabled = it.toBooleanLenient() ?: error("Invalid value for enabled") },
            ConfigurationOption(
                PluginConfiguration::unusedWarnAsError.name,
                "<true|false>",
                "Whether checks should be applied as errors",
            ) {
                PluginConfiguration.unusedWarnAsError =
                    it.toBooleanLenient() ?: error("Invalid value for warnOnly")
            },
            ConfigurationOption(
                PluginConfiguration::unusedSignatures.name,
                "<*.@CheckReturnValue|*.@Pure|...>",
                "List of annotation or types to check for unused returned values",
            ) {
                var sigs = it.toConfigSet(PluginConfiguration.unusedSignatures) + "*.@CheckReturnValue"
                PluginConfiguration.unusedSignatures = sigs
            },
            ConfigurationOption(
                PluginConfiguration::unusedPresetLibs.name,
                "<kotlin-stdlib>",
                "Presets to load",
            ) {
                PluginConfiguration.unusedPresetLibs = it.toConfigSet(PluginConfiguration.unusedPresetLibs) + "default"
            },

            //Unsafe
            ConfigurationOption(
                PluginConfiguration::unsafeEnabled.name,
                "<true|false>",
                "Whether the plugin is enabled or not",
            ) { PluginConfiguration.unsafeEnabled = it.toBooleanLenient() ?: error("Invalid value for enabled") },
            ConfigurationOption(
                PluginConfiguration::unsafeWarnAsError.name,
                "<true|false>",
                "Whether checks should be applied as errors",
            ) {
                PluginConfiguration.unsafeWarnAsError =
                    it.toBooleanLenient() ?: error("Invalid value for warnOnly")
            },
            ConfigurationOption(
                PluginConfiguration::unsafeSignatures.name,
                "<kotlin.Array.get(kotlin.Int)>",
                "List of unsafe functions",
            ) { PluginConfiguration.unsafeSignatures = it.toConfigSet(PluginConfiguration.unsafeSignatures) },
            ConfigurationOption(
                PluginConfiguration::unsafePresetLibs.name,
                "<kotlin-stdlib>",
                "Presets to load",
            ) { PluginConfiguration.unsafePresetLibs = it.toConfigSet(PluginConfiguration.unsafePresetLibs) }
        )
    }
}
