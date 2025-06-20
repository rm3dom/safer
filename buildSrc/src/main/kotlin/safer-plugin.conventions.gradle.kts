import java.nio.file.Paths
import java.util.Base64

plugins {
    id("safer-library.conventions")
    `maven-publish`
    signing
    id("com.gradleup.shadow")
}

val enablePublishing = boolProperty("safer.publish", false)
val isSnapshot = version.toString().contains("SNAPSHOT", ignoreCase = true)

val distPublishDir =
    if(isSnapshot)
        layout.buildDirectory.dir("maven/snapshot").get()
    else
        layout.buildDirectory.dir("maven/publish").get()

val distPublishUrl = Paths.get(distPublishDir.toString(), "repository").toUri().toString()

val isGradlePlugin = project.name == "safer-gradle-plugin"

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

fun configureEmptyJavadocArtifact(): org.gradle.jvm.tasks.Jar {
    val javadocJar by project.tasks.creating(Jar::class) {
        archiveClassifier.set("javadoc")
        // contents are deliberately left empty
        // https://central.sonatype.org/publish/requirements/#supply-javadoc-and-sources
    }
    return javadocJar
}

fun configureSourcesArtifact() : org.gradle.jvm.tasks.Jar {
    val sourcesJar by tasks.creating(Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }
    return sourcesJar
}


publishing {
    //val isSnapshot = version.toString().contains("SNAPSHOT", ignoreCase = true)
    //val repositoryUrl = if (isSnapshot) {
    //    "https://central.sonatype.com/repository/maven-snapshots/"
    //} else {
    //    "https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/"
    //}

    publications {
        repositories {
            mavenLocal()

            if (enablePublishing) {
                maven {
                    name = "local-staging"
                    setUrl(distPublishUrl)
                    //credentials {
                    //    username = stringProperty("MVN_CENTRAL_USERNAME", "bad")
                    //    password = stringProperty("MVN_CENTRAL_PASSWORD", "bad")
                    //}
                }
            }
        }

        val javadocJar = if (!isGradlePlugin) configureEmptyJavadocArtifact() else null
        val sourcesJar = if (!isGradlePlugin) configureSourcesArtifact() else null

        create<MavenPublication>("mavenJava") {
            //From the shadow jar, embed common and compiler plugin in maven
            from(components["shadow"])
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            artifact(javadocJar)
            artifact(sourcesJar)

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

val packageMavenArtifacts by tasks.registering(Zip::class) {
    from(distPublishUrl)
    archiveFileName.set("${project.name}-artifacts.zip")
    destinationDirectory.set(distPublishDir)
    dependsOn(tasks.publish)
}



val uploadMavenArtifacts by tasks.registering {
    dependsOn(packageMavenArtifacts)

    doLast {
        val uriBase = "https://central.sonatype.com/api/v1/publisher/upload"
        //val uriBase = "http://localhost:8080/api/v1/publisher/upload"
        val publishingType = "USER_MANAGED"
        val deploymentName = "${project.name}-$version"
        val uri = "$uriBase?name=$deploymentName&publishingType=$publishingType"

        val userName = stringProperty("MVN_CENTRAL_USERNAME", "bad")
        val password = stringProperty("MVN_CENTRAL_PASSWORD", "bad")
        val base64Auth = Base64.getEncoder().encode("$userName:$password".toByteArray()).toString(Charsets.UTF_8)
        val bundleFile = packageMavenArtifacts.get().archiveFile.get().asFile

        println("Sending request to $uri...")

        val statusCode = FileUploader.uploadFile(uri, "Bearer $base64Auth", bundleFile, "bundle")
        if (statusCode != 201) {
            error("Upload error to Central repository. Status code $statusCode.")
        }
    }
}




