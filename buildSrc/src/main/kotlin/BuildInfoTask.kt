import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.concurrent.TimeUnit


private fun String.runCommand(
    workingDir: File = File("."),
    timeoutAmount: Long = 60,
    timeoutUnit: TimeUnit = TimeUnit.SECONDS
): String {
    return try {
        ProcessBuilder(split("\\s(?=(?:[^'\"`]*(['\"`])[^'\"`]*\\1)*[^'\"`]*$)".toRegex()))
            .directory(workingDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()
            .apply {
                if (!waitFor(timeoutAmount, timeoutUnit)) {
                    destroy()
                    throw RuntimeException("Command timed out: $this")
                }
            }
            .run {
                val error = errorStream.bufferedReader().readText().trim()
                if (error.isNotEmpty() && exitValue() != 0) {
                    throw RuntimeException("Command failed with exit code ${exitValue()}: $error")
                }
                inputStream.bufferedReader().readText().trim()
            }
    } catch (e: Exception) {
        if (e is RuntimeException) throw e
        throw RuntimeException("Failed to execute command: $this", e)
    }
}

interface BuildProp {
    val name: String
    val value: Any

    data class StringValue(override val name: String, override val value: String) : BuildProp
    data class BooleanValue(override val name: String, override val value: Boolean) : BuildProp
    data class IntValue(override val name: String, override val value: Int) : BuildProp
    data class LongValue(override val name: String, override val value: Long) : BuildProp
}

fun BuildProp(name: String, value: String) = BuildProp.StringValue(name, value)
fun BuildProp(name: String, value: Int) = BuildProp.IntValue(name, value)
fun BuildProp(name: String, value: Long) = BuildProp.LongValue(name, value)
fun BuildProp(name: String, value: Boolean) = BuildProp.BooleanValue(name, value)


open class BuildInfoTask : DefaultTask() {

    @get:Input
    val buildProperties: ListProperty<BuildProp> = project.objects.listProperty<BuildProp>(BuildProp::class.java)

    @get:Input
    val packageName: Property<String> = project.objects.property<String>(String::class.java)


    private fun String.toBooleanOrNull() =
        when(lowercase()) {
           "yes", "y", "true" -> true
           else -> false
        }

    @TaskAction
    fun run() {
        require(packageName.get().isNotBlank()) { "packageName must be set" }

        //TODO write to build/generated

        // Find the source folder
        val sourceFolder = listOf("src/commonMain/kotlin", "src/jvmMain/kotlin", "src/main/kotlin")
            .map { project.layout.projectDirectory.dir(it) }
            .firstOrNull { it.asFile.exists() }
            ?: error("Cannot find kotlin source folder")

        // Create the package directory
        val dirName = packageName.get().replace('.', '/').trim('/')
        val dir = sourceFolder.dir(dirName).asFile
        val sourceFile = sourceFolder.dir("$dirName/BuildInfo.kt").asFile
        dir.mkdirs()

        // Get Git revision or use a placeholder if Git is not available
        val gitRev = try {
            "git rev-parse HEAD".runCommand(workingDir = project.rootDir)
        } catch (e: Exception) {
            logger.warn("Failed to get Git revision: ${e.message}")
            "unknown"
        }

        // Determine if we're in development mode
        val devMode = project.properties["development"].toString().toBooleanOrNull() == true

        // Combine default properties with custom properties
        val props = listOf(
            BuildProp("projectName", project.name.toString()),
            BuildProp("projectGroup", project.group.toString()),
            BuildProp("projectId", "${project.group}.${project.name}"),
            BuildProp("development", devMode),
            BuildProp("projectVersion", project.version.toString()),
            BuildProp("buildTime", System.currentTimeMillis() / 1000),
            BuildProp("gitRev", gitRev),
        ) + buildProperties.get()

        // Generate the properties string
        val stringProps = props.joinToString("") { prop ->
            when (prop) {
                is BuildProp.StringValue -> "    const val ${prop.name} = \"${prop.value}\"\n"
                else -> "    const val ${prop.name} = ${prop.value}\n"
            }
        }

        // Write the BuildInfo.kt file
        sourceFile.parentFile.mkdirs() // Ensure parent directories exist
        sourceFile.writeText(
            """package ${packageName.get()}

// Auto-generated file. Do not edit!
@Suppress("MagicNumber")
internal object BuildInfo {
$stringProps}
"""
        )
    }
}

/**
 * Generates a BuildInfo.kt file with build-related properties.
 *
 * @param packageName The package name for the generated BuildInfo class.
 * @param props Additional build properties to include in the BuildInfo class.
 */
fun Project.generateBuildInfo(packageName: String, vararg props: BuildProp) {
    val buildInfo = tasks.register("build-info", BuildInfoTask::class.java) {
        buildProperties.set(props.toList())
        this.packageName.set(packageName)
        description = "Generates BuildInfo.kt file with build-related properties"
        group = "build"
    }

    // Make compileKotlin depend on build-info
    tasks.matching { it.name.startsWith("compileKotlin") }.configureEach {
        dependsOn(buildInfo)
    }
}
