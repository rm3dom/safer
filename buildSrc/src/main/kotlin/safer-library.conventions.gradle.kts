import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    `java-library`
}

val versionCatalog = the<VersionCatalogsExtension>().named("libs")
val kotlinVersion = versionCatalog.findVersion("kotlin").get().requiredVersion
val saferVersion = stringProperty("safer.version", "0.1")

group = "com.swiftleap"
version = "$kotlinVersion-${saferVersion}"

description = when (project.name) {
    "safer-gradle-plugin" -> "Safer Gradle plugin"
    "safer-maven-plugin" -> "Safer Maven plugin"
    "safer-compiler-plugin" -> "Safer compiler plugin"
    else -> project.name
} + ". Better Safer than, sorry."

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
    options.release.set(8)
}