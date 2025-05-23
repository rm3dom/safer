import org.gradle.kotlin.dsl.withType

plugins {
    id("safer-library.conventions")
    `maven-publish`
    signing
    id("com.gradleup.shadow")
}

val enablePublishing = boolProperty("safer.publish", false)

dependencies {
    implementation(project(":safer-common"))
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

tasks.publish { dependsOn(tasks.shadowJar) }

publishing {
    val isSnapshot = version.toString().contains("SNAPSHOT", ignoreCase = true)
    val repositoryUrl = if (isSnapshot) {
        "https://central.sonatype.com/repository/maven-snapshots/"
    } else {
        "https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/"
    }

    publications {
        create<MavenPublication>("mavenJava") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            //From the shadow jar, embed common and compiler plugin in maven
            from(components["shadow"])

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
                        username = stringProperty("MVN_CENTRAL_USERNAME", "bad")
                        password = stringProperty("MVN_CENTRAL_PASSWORD", "bad")
                    }
                }
            }
        }
    }
}

signing {
    isRequired = enablePublishing
    if (enablePublishing) {
        val signingKey = stringProperty("GPG_KEY", "bad")
        val signingPassword = stringProperty("GPG_KEY_PASSWORD", "bad")
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications["mavenJava"])
    }
}

if (enablePublishing) {
    tasks.withType<AbstractPublishToMaven>().configureEach {
        mustRunAfter(tasks.withType<Sign>())
    }
}