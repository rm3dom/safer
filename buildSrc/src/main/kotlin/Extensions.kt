import org.gradle.api.Project


fun Project.stringProperty(name: String, default: String): String {
    val prop = try {
        property(name)
    } catch (_: Exception) {
        null
    }
    return prop?.toString()
        ?: System.getProperty(name)
        ?: System.getenv(name)
        ?: default
}


fun Project.boolProperty(name: String, default: Boolean): Boolean =
    stringProperty(name, default.toString()).toBoolean()