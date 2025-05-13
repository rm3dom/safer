plugins {
    kotlin("jvm")
    `java-library`
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

dependencies {
    implementation(libs.kotlin.compiler.embeddable)
    testImplementation(libs.kotlin.test)
}

publishing {
    publications {
        named<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}

generateBuildInfo(
    "com.swiftleap.safer.plugin",
    BuildProp("kotlinVersion", libs.versions.kotlin.get())
)
