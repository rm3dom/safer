
plugins {
    id("safer-plugin.conventions")
    `java-gradle-plugin`
    alias(libs.plugins.gradle.plugin.publish)
}

dependencies {
    compileOnly(project(":safer-compiler-plugin"))
    compileOnly(libs.kotlin.gradle.plugin.api)
}

gradlePlugin {
    vcsUrl = "https://github.com/rm3dom/safer.git"
    website = "https://github.com/rm3dom/safer"
    plugins {
        create("SaferGradlePlugin") {
            id = "com.swiftleap.safer"
            displayName = "Safer Gradle plugin"
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
