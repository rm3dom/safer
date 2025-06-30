plugins {
    kotlin("jvm") apply false
}

val buildTool = stringProperty("safer.buildTool", "")
val saferVersion = stringProperty("safer.version", "")
val isSnapshot = saferVersion.contains("SNAPSHOT", true)
val enablePublishing = boolProperty("safer.publish", false) && !isSnapshot

require(buildTool in listOf("maven", "gradle")) {
    "safer.buildTool must be one of maven, gradle, but was '$buildTool' instead."
}

tasks.create("gradle-dev-publish") {
    dependsOn(":safer-compiler-plugin:publishToMavenLocal", ":safer-gradle-plugin:publishToMavenLocal")
}

tasks.create("maven-dev-publish") {
    dependsOn(":safer-maven-plugin:publishToMavenLocal")
}

tasks.create("compiler-plugin-publish") {
    enabled = enablePublishing
    dependsOn(
        ":safer-compiler-plugin:uploadMavenArtifacts",
    )
}

tasks.create("gradle-plugin-publish") {
    enabled = enablePublishing
    dependsOn(":safer-gradle-plugin:publishPlugins")
}

tasks.create("maven-plugin-publish") {
    enabled = enablePublishing
    dependsOn(
        ":safer-maven-plugin:uploadMavenArtifacts"
    )
}