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

tasks.create("maven-dev-publish") {
    dependsOn(":safer-maven-plugin:publishToMavenLocal")
}

tasks.create("gradle-plugin-publish") {
    if(enablePublishing) {
        dependsOn(":safer-gradle-plugin:publish")
        if(!saferVersion.contains("SNAPSHOT", true))
            dependsOn(":safer-gradle-plugin:publishPlugins")
    }
}

