plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

dependencies {
    compileOnly(libs.kotlin.maven.plugin)
    compileOnly(libs.maven.core)
}

publishing {
    publications {
        named<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}

generateBuildInfo(
    "com.swiftleap.safer",
    BuildProp("compilerPluginName", project(":safer-compiler-plugin").name)
)
