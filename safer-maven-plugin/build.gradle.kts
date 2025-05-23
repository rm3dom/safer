plugins {
    id("safer-plugin.conventions")
}

dependencies {
    implementation(project(":safer-compiler-plugin"))
    compileOnly(libs.kotlin.maven.plugin)
    compileOnly(libs.maven.core)
}


requiresBuildTool("maven")