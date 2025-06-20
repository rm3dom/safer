plugins {
    kotlin("jvm") apply false
}

val buildTool = stringProperty("safer.buildTool", "")
val saferVersion = stringProperty("safer.version", "")
val enablePublishing = boolProperty("safer.publish", false)

require(buildTool in listOf("maven", "gradle")) {
    "safer.buildTool must be one of maven, gradle, but was '$buildTool' instead."
}

tasks.create("gradle-dev-publish") {
    dependsOn(":safer-compiler-plugin:publishToMavenLocal", ":safer-gradle-plugin:publishToMavenLocal")
}

tasks.create("gradle-plugin-publish") {
    if (enablePublishing) {
        if (!saferVersion.contains("SNAPSHOT", true))
            dependsOn(":safer-gradle-plugin:publishPlugins")
    }
}

tasks.create("gradle-publish") {
    if (enablePublishing) {
        if (!saferVersion.contains("SNAPSHOT", true))
            dependsOn(
                ":safer-compiler-plugin:uploadMavenArtifacts",
                ":safer-gradle-plugin:publishPlugins"
            )
    }
}

tasks.create("maven-dev-publish") {
    dependsOn(":safer-maven-plugin:publishToMavenLocal")
}

tasks.create("maven-publish") {
    if (enablePublishing) {
        if (!saferVersion.contains("SNAPSHOT", true))
            dependsOn(
                ":safer-maven-plugin:uploadMavenArtifacts"
            )
    }
}