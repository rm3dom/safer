pluginManagement {
    repositories {
        // Use the plugin portal to apply community plugins in convention plugins.
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

rootProject.name = "safer"

include(
    ":safer-compiler-plugin",
    ":safer-gradle-plugin"
)
