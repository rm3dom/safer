package com.swiftleap.safer

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

/**
 * Gradle plugin for the Safer compiler plugin.
 *
 * This plugin integrates the Safer compiler plugin with Gradle, allowing users
 * to configure the plugin through their Gradle build scripts.
 */
internal class SaferGradlePlugin : KotlinCompilerPluginSupportPlugin {

    /**
     * Applies the plugin to the target project.
     *
     * Creates a 'safer' extension in the project to allow configuration
     * of the Safer compiler plugin through the Gradle build script.
     *
     * @param target The target project
     */
    override fun apply(target: Project) {
        target.extensions.create( // add a configuration object to the Gradle file
            "safer",
            SaferConfiguration::class.java
        )
    }

    private fun Iterable<String>.joinToConfigString() = joinToString("&")

    /**
     * Applies the plugin to a specific Kotlin compilation.
     *
     * Converts the plugin configuration from the Gradle extension into
     * compiler plugin options that will be passed to the Kotlin compiler.
     *
     * @param kotlinCompilation The Kotlin compilation
     * @return A provider of subplugin options
     */
    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project

        val config = project.extensions.findByType(SaferConfiguration::class.java)
            ?: SaferConfiguration()

        val parameters = mutableListOf<SubpluginOption>()

        (config.unusedEnabled)?.let {
            parameters += SubpluginOption(
                SaferConfiguration::unusedEnabled.name,
                it.toString()
            )
        }

        (config.unusedWarnAsError)?.let {
            parameters += SubpluginOption(
                SaferConfiguration::unusedWarnAsError.name,
                it.toString()
            )
        }

        (config.unusedSignatures)?.let {
            parameters += SubpluginOption(
                SaferConfiguration::unusedSignatures.name,
                it.joinToConfigString()
            )
        }

        (config.unusedPresetLibs)?.let {
            parameters += SubpluginOption(
                SaferConfiguration::unusedPresetLibs.name,
                it.joinToConfigString()
            )
        }


        (config.unsafeEnabled)?.let {
            parameters += SubpluginOption(
                SaferConfiguration::unsafeEnabled.name,
                it.toString()
            )
        }

        (config.unsafeWarnAsError)?.let {
            parameters += SubpluginOption(
                SaferConfiguration::unsafeWarnAsError.name,
                it.toString()
            )
        }

        (config.unsafeSignatures)?.let {
            parameters += SubpluginOption(
                SaferConfiguration::unsafeSignatures.name,
                it.joinToConfigString()
            )
        }

        (config.unsafePresetLibs)?.let {
            parameters += SubpluginOption(
                SaferConfiguration::unsafePresetLibs.name,
                it.joinToConfigString()
            )
        }

        return project.provider { parameters }
    }

    /**
     * Gets the ID of the compiler plugin.
     *
     * This ID is used to identify the compiler plugin in the Kotlin compiler.
     *
     * @return The compiler plugin ID
     */
    override fun getCompilerPluginId(): String = "${BuildInfo.projectGroup}.${BuildInfo.compilerPluginName}"

    /**
     * Gets the artifact information for the Kotlin compiler plugin.
     *
     * This information is used to resolve the compiler plugin JAR from repositories.
     *
     * @return The subplugin artifact information
     */
    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        BuildInfo.projectGroup,
        BuildInfo.compilerPluginName,
        //TODO resolve to the correct supported plugin version
        BuildInfo.projectVersion,
    )

    /**
     * Determines if this plugin is applicable to the given Kotlin compilation.
     *
     * The Safer plugin is applicable to all Kotlin compilations.
     *
     * @param kotlinCompilation The Kotlin compilation
     * @return true, as this plugin is applicable to all Kotlin compilations
     */
    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true
}
