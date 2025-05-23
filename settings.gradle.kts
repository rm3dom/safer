pluginManagement {
    repositories {
        // Use the plugin portal to apply community plugins in convention plugins.
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        mavenLocal()
    }
}

rootProject.name = "safer"

include(
    "safer-common",
    "safer-compiler-plugin",
    "safer-gradle-plugin",
    "safer-maven-plugin"
)
