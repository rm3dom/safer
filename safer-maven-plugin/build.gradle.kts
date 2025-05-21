plugins {
    kotlin("jvm")
    `maven-publish`
    id("com.gradleup.shadow") version "8.3.6"
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}


val buildTool = stringProperty("safer.buildTool", "")

dependencies {
    implementation(project(":safer-compiler-plugin"))
    compileOnly(libs.kotlin.maven.plugin)
    compileOnly(libs.maven.core)
}

val validateBuild = tasks.create("validate-build") {
    doFirst {
        if(buildTool != "maven")
            error("""
                ######################################################
                Maven build disabled, please set -P "safer.buildTool=maven"
                ######################################################
            """.trimIndent())
    }
}

tasks.shadowJar { dependsOn(validateBuild) }
tasks.compileKotlin { dependsOn(validateBuild) }

tasks.publish {
    dependsOn(validateBuild, tasks.shadowJar)
}

tasks.shadowJar {
    archiveClassifier.set("")
    dependencies {
        exclude {
            it.moduleGroup == "org.jetbrains"
            it.moduleGroup == "org.jetbrains.kotlin"
        }
    }
}

publishing {
    publications {
        named<MavenPublication>("mavenJava") {
            from(components["shadow"])
        }
    }
}