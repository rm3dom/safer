package com.swiftleap.safer

/**
 * Configuration class for the Safer Gradle plugin.
 *
 * This class provides a DSL for configuring the Safer compiler plugin
 * through the Gradle build script.
 */
open class SaferConfiguration {
    /**
     * Whether the unused return value checking is enabled.
     */
    internal var unusedEnabled: Boolean? = null

    /**
     * Whether unused return value warnings should be treated as errors.
     */
    internal var unusedWarnAsError: Boolean? = null

    /**
     * Set of signature strings for functions or types that should have their return values used.
     */
    internal var unusedSignatures: MutableSet<String>? = null

    /**
     * Set of preset library names to load unused signatures from.
     */
    internal var unusedPresetLibs: MutableSet<String>? = null

    /**
     * Configuration class for the unused return value checking feature.
     *
     * This class provides methods for configuring how the plugin checks
     * for unused return values.
     */
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
         * Sets whether unused return value warnings should be treated as errors.
         *
         * @param enabled Whether to treat warnings as errors (default: true)
         */
        fun warnAsError(enabled: Boolean = true) {
            unusedWarnAsError = enabled
        }

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
    fun unused(action: UnusedConfiguration.() -> Unit) {
        action(UnusedConfiguration())
    }

    /**
     * Whether the unsafe function checking is enabled.
     */
    internal var unsafeEnabled: Boolean? = null

    /**
     * Whether unsafe function warnings should be treated as errors.
     */
    internal var unsafeWarnAsError: Boolean? = null

    /**
     * Set of signature strings for functions that are considered unsafe.
     */
    internal var unsafeSignatures: MutableSet<String>? = null

    /**
     * Set of preset library names to load unsafe function signatures from.
     */
    internal var unsafePresetLibs: MutableSet<String>? = null

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
         * Sets whether unsafe function warnings should be treated as errors.
         *
         * @param enabled Whether to treat warnings as errors (default: true)
         */
        fun warnAsError(enabled: Boolean = true) {
            unsafeWarnAsError = enabled
        }

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
