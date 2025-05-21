package com.swiftleap.safer

import org.apache.maven.plugin.*
import org.apache.maven.project.*
import org.codehaus.plexus.component.annotations.*
import org.codehaus.plexus.logging.*
import org.jetbrains.kotlin.maven.*


@Component(
    role = KotlinMavenPluginExtension::class,
    hint = "com.swiftleap.safer",
    version = BuildInfo.projectVersion,
)
class SaverMavenPluginExtension : KotlinMavenPluginExtension {
    @Requirement
    lateinit var logger: Logger

    override fun getCompilerPluginId() = "${BuildInfo.projectGroup}.${BuildInfo.compilerPluginName}"

    override fun isApplicable(project: MavenProject, execution: MojoExecution) = true

    override fun getPluginOptions(project: MavenProject, execution: MojoExecution): List<PluginOption> {
        logger.debug("Loaded Maven plugin " + javaClass.name)
        return emptyList()
    }
}
