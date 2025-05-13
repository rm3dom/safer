import org.gradle.api.Project


fun Project.boolProperty(name: String, default: Boolean): Boolean = property(name)?.toString()?.toBoolean() ?: default
fun Project.stringProperty(name: String, default: String): String = property(name)?.toString() ?: default