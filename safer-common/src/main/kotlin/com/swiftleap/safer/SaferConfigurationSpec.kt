@file:MustUseReturnValue
package com.swiftleap.safer

import org.jetbrains.annotations.Contract
import kotlin.reflect.KProperty0

interface SaferConfigurationSpec {
    val unusedEnabled: Boolean
    val unusedWarnAsError: Boolean
    val unusedSignatures: Set<String>
    val unusedPresetLibs: Set<String>
    val unsafeEnabled: Boolean
    val unsafeWarnAsError: Boolean
    val unsafeSignatures: Set<String>
    val unsafePresetLibs: Set<String>

    companion object {
        const val SEPERATOR = ";"
    }
}

data class SaferConfiguration(
    override val unusedEnabled: Boolean,
    override val unusedWarnAsError: Boolean,
    override val unusedSignatures: Set<String>,
    override val unusedPresetLibs: Set<String>,
    override val unsafeEnabled: Boolean,
    override val unsafeWarnAsError: Boolean,
    override val unsafeSignatures: Set<String>,
    override val unsafePresetLibs: Set<String>,
) : SaferConfigurationSpec

val BuildInfo.compilerProjectId get() = "${BuildInfo.projectGroup}.${BuildInfo.compilerPluginName}"

/**
 * Converts a set of strings to a configuration string.
 * Joins the set elements with a comma and space separator.
 *
 * @return A string representation of the set for configuration purposes
 */
@Contract(pure = true)
fun Set<String>.toPluginConfigString(): String =
    joinToString(separator = SaferConfigurationSpec.SEPERATOR)

/**
 * Converts a string to a set of configuration values.
 * Splits the string by commas, spaces, or colons and returns the resulting set.
 * If the resulting set is empty, returns the default set.
 *
 * @param default The default set to return if the resulting set is empty
 * @return A set of configuration values
 */
@Contract(pure = true)
fun String.toPluginConfigSet(default: Set<String>): Set<String> {
    val set = this.trim('\'', '"')
        .split(SaferConfigurationSpec.SEPERATOR)
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .toSet()
    return set.ifEmpty { default }
}

/**
 * Creates a plugin CLI option string for a boolean property.
 *
 * @param value The value to use (defaults to the current property value)
 * @return A string in the format "plugin:projectId:propertyName=value"
 */
@Contract(pure = true)
fun KProperty0<Boolean>.asPluginCliOption(value: Boolean = get()): String =
    "plugin:${BuildInfo.compilerProjectId}:${name}=${value}"

/**
 * Creates a plugin CLI option string for a Set<String> property.
 *
 * @param value The value to use (defaults to the current property value)
 * @return A string in the format "plugin:projectId:propertyName=value1, value2, ..."
 */
@Contract(pure = true)
fun KProperty0<Set<String>>.asPluginCliOption(value: Set<String> = get()): String =
    "plugin:${BuildInfo.compilerProjectId}:${name}=\'${value.toPluginConfigString()}\'"

/**
 * Configuration class for the Safer Gradle plugin.
 *
 * This class provides a DSL for configuring the Safer compiler plugin
 * through the Gradle build script.
 */
open class SaferConfigurationBuilder {
    private var unusedEnabled: Boolean? = null
    private var unusedWarnAsError: Boolean? = null
    private var unusedSignatures: MutableSet<String>? = null
    private var unusedPresetLibs: MutableSet<String>? = null
    private var unsafeEnabled: Boolean? = null
    private var unsafeWarnAsError: Boolean? = null
    private var unsafeSignatures: MutableSet<String>? = null
    private var unsafePresetLibs: MutableSet<String>? = null

    fun build() : SaferConfiguration = SaferConfiguration(
        unusedEnabled = unusedEnabled ?: true,
        unusedWarnAsError = unusedWarnAsError ?: false,
        unusedSignatures = unusedSignatures ?: mutableSetOf(),
        unusedPresetLibs = unusedPresetLibs ?: mutableSetOf(),
        unsafeEnabled = unsafeEnabled ?: true,
        unsafeWarnAsError = unsafeWarnAsError ?: false,
        unsafeSignatures = unsafeSignatures ?: mutableSetOf(),
        unsafePresetLibs = unsafePresetLibs ?: mutableSetOf()
    )

    /**
     * Configuration class for the unused return value checking feature.
     *
     * This class provides methods for configuring how the plugin checks
     * for unused return values.
     */
    @Deprecated("Use kotlin 2.2.0 @file:MustUseReturnValue.")
    inner class UnusedConfiguration {
        /**
         * Enables or disables the unused return value checking.
         *
         * @param enabled Whether to enable the feature (default: true)
         */
        fun enabled(enabled: Boolean = true) {
            unusedEnabled = enabled
        }

        /**
         * Enables or disables the unused return value checking.
         */
        var enabled
            set(value) = enabled(value)
            get() = unusedEnabled ?: true

        /**
         * Sets whether unused return value warnings should be treated as errors.
         *
         * @param enabled Whether to treat warnings as errors (default: true)
         */
        fun warnAsError(enabled: Boolean = true) {
            unusedWarnAsError = enabled
        }

        /**
         * Sets whether unused return value warnings should be treated as errors.
         */
        var warnAsError
            set(value) = warnAsError(value)
            get() = unusedWarnAsError ?: true

        /**
         * Adds the Kotlin standard library to the list of libraries to check.
         * This will check that return values from Kotlin standard library functions are used.
         */
        fun checkKotlinStdLib() = addLib("kotlin-stdlib")

        /**
         * Adds the Kotlinx coroutines core library to the list of libraries to check.
         */
        fun checkKotlinCoroutines() = addLib("kotlin-coroutines")

        /**
         * Adds the Java standard library to the list of libraries to check.
         * This will check that return values from Java standard library functions are used.
         */
        fun checkJavaExperimental() = addLib("java")

        /**
         * Adds signatures to check for unused return values.
         *
         * @param signature One or more signature strings to check
         */
        fun checkSignatures(vararg signature: String) {
            unusedSignatures = unusedSignatures ?: mutableSetOf()
            unusedSignatures?.addAll(signature)
        }

        /**
         * Adds a library to the list of preset libraries to check.
         *
         * @param lib The name of the library to add
         */
        private fun addLib(lib: String) {
            unusedPresetLibs = unusedPresetLibs ?: mutableSetOf()
            unusedPresetLibs?.add(lib)
        }
    }

    /**
     * Configures the unused return value checking feature.
     *
     * @param action A configuration block for the unused return value checking
     */
    @Deprecated("Use kotlin 2.2.0 @file:MustUseReturnValue.")
    @Suppress("DEPRECATION")
    fun unused(action: UnusedConfiguration.() -> Unit) {
        action(UnusedConfiguration())
    }

    /**
     * Configuration class for the unsafe function checking feature.
     *
     * This class provides methods for configuring how the plugin checks
     * for usage of unsafe functions.
     */
    inner class UnsafeConfiguration {
        /**
         * Enables or disables the unsafe function checking.
         *
         * @param enabled Whether to enable the feature (default: true)
         */
        fun enabled(enabled: Boolean = true) {
            unsafeEnabled = enabled
        }

        /**
         * Enables or disables the unsafe function checking.
         */
        var enabled
            set(value) = enabled(value)
            get() = unsafeEnabled ?: true

        /**
         * Sets whether unsafe function warnings should be treated as errors.
         *
         * @param enabled Whether to treat warnings as errors (default: true)
         */
        fun warnAsError(enabled: Boolean = true) {
            unsafeWarnAsError = enabled
        }

        /**
         * Sets whether unsafe function warnings should be treated as errors.
         */
        var warnAsError
            set(value) = warnAsError(value)
            get() = unsafeWarnAsError ?: true

        /**
         * Adds the Kotlin standard library to the list of libraries to check.
         * This will check for usage of unsafe functions from the Kotlin standard library.
         */
        fun checkKotlinStdLib() = addLib("kotlin-stdlib")

        /**
         * Adds the Kotlinx coroutines core library to the list of libraries to check.
         */
        fun checkKotlinCoroutines() = addLib("kotlin-coroutines")

        /**
         * Adds the Java standard library to the list of libraries to check.
         * This will check for usage of unsafe functions from the Java standard library.
         */
        fun checkJavaExperimental() = addLib("java")

        /**
         * Adds signatures of functions that should be considered unsafe.
         *
         * @param signature One or more signature strings of unsafe functions
         */
        fun checkSignatures(vararg signature: String) {
            unsafeSignatures = unsafeSignatures ?: mutableSetOf()
            unsafeSignatures?.addAll(signature)
        }

        /**
         * Adds a library to the list of preset libraries to check for unsafe functions.
         *
         * @param lib The name of the library to add
         */
        private fun addLib(lib: String) {
            unsafePresetLibs = unsafePresetLibs ?: mutableSetOf()
            unsafePresetLibs?.add(lib)
        }
    }

    /**
     * Configures the unsafe function checking feature.
     *
     * @param action A configuration block for the unsafe function checking
     */
    fun unsafe(action: UnsafeConfiguration.() -> Unit) {
        action(UnsafeConfiguration())
    }
}