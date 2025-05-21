import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.nio.file.Paths


plugins {
    `maven-publish`
    signing
    kotlin("multiplatform") apply false
}

val enablePublishing = boolProperty("safer.publish", false)
val kotlinVersion = libs.versions.kotlin.get()
val saferVersion = stringProperty("safer.version", "0.1")
val buildTool = stringProperty("safer.buildTool", "")

require(buildTool in listOf("maven", "gradle")) {
    "safer.buildTool must be one of maven, gradle, but was '$buildTool' instead."
}

tasks.create("gradle-dev-publish") {
    dependsOn(":safer-compiler-plugin:publishToMavenLocal", ":safer-gradle-plugin:publishToMavenLocal")
}

tasks.create("maven-dev-publish") {
    dependsOn(":safer-maven-plugin:publishToMavenLocal")
}

subprojects {
    apply(plugin = "maven-publish")

    if (enablePublishing) apply(plugin = "signing")

    group = "com.swiftleap"

    description = when (project.name) {
        "safer-gradle-plugin" -> "Safer Gradle plugin"
        "safer-maven-plugin" -> "Safer Maven plugin"
        "safer-compiler-plugin" -> "Safer compiler plugin"
        else -> project.name
    } + ". Better safe than, sorry."

    version = "$kotlinVersion-${saferVersion}"

    repositories {
        mavenCentral()
        mavenLocal()
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

    generateBuildInfo(
        "com.swiftleap.safer",
        BuildProp("compilerPluginName", project(":safer-compiler-plugin").name),
        BuildProp("kotlinVersion", kotlinVersion)
    )

    val repositoryUrl = if (version.toString().endsWith("SNAPSHOT")) {
        "https://central.sonatype.com/repository/maven-snapshots/"
    } else {
        "https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/"
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                groupId = project.group.toString()
                artifactId = project.name
                version = project.version.toString()

                pom {
                    name.set(project.name)
                    description.set(project.description)
                    url.set("https://github.com/rm3dom/safer")
                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://mit-license.org/")
                        }
                    }
                    developers {
                        developer {
                            id.set("rm3dom")
                            name.set("Ruan Strydom")
                            email.set("rm3dom@gmail.com")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/rm3dom/safer.git")
                        developerConnection.set("scm:git:ssh://github.com/rm3dom/safer.git")
                        url.set("https://github.com/rm3dom/safer.git")
                    }
                }
            }

            repositories {
                mavenLocal()

                if (enablePublishing) {
                    maven {
                        name = "MavenCentral"
                        setUrl(repositoryUrl)
                        credentials {
                            username = System.getenv("MVN_CENTRAL_USERNAME")
                            password = System.getenv("MVN_CENTRAL_PASSWORD")
                        }
                    }
                }
            }
        }
    }

    if (enablePublishing) {
        tasks.withType<AbstractPublishToMaven>().configureEach {
            mustRunAfter(tasks.withType<Sign>())
        }

        signing {
            val signingKey = System.getenv("GPG_KEY")
            val signingPassword = System.getenv("GPG_KEY_PASSWORD")
            useInMemoryPgpKeys(signingKey, signingPassword)
            sign(publishing.publications["mavenJava"])
        }
    }
}
