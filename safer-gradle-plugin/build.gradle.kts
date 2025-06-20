plugins {
    id("safer-plugin.conventions")
    `java-gradle-plugin`
    alias(libs.plugins.gradle.plugin.publish)
}

dependencies {
    compileOnly(project(":safer-compiler-plugin"))
    compileOnly(libs.kotlin.gradle.plugin.api)
}

ext["gradle.publish.key"] = stringProperty("GRADLE_KEY", "bad")
ext["gradle.publish.secret"] = stringProperty("GRADLE_SECRET", "bad")

// Multiple publications with coordinates 'com.swiftleap:safer-gradle-plugin:2.1.20-0.3-SNAPSHOT' are published to repository 'MavenLocal'.
// The publications 'mavenJava' in project ':safer-gradle-plugin' and 'pluginMaven' in project ':safer-gradle-plugin' will overwrite each other!

gradlePlugin {
    vcsUrl = "https://github.com/rm3dom/safer.git"
    website = "https://github.com/rm3dom/safer"
    plugins {
        create("SaferGradlePlugin") {
            id = "com.swiftleap.safer"
            displayName = "Safer Gradle plugin"
            version = project.version as String
            vcsUrl = "https://github.com/rm3dom/safer.git"
            description = project.description
            implementationClass = "com.swiftleap.safer.SaferGradlePlugin"
            tags = listOf(
                "kotlin",
                "compiler-plugin"
            )
        }
    }
}

requiresBuildTool("gradle")
