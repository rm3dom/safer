plugins {
    kotlin("jvm")
    `java-gradle-plugin`
    alias(libs.plugins.gradle.plugin.publish)
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

dependencies {
    compileOnly(project(":safer-compiler-plugin"))
    compileOnly(libs.kotlin.gradle.plugin.api)
}

publishing {
    publications {
        named<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
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
