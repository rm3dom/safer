plugins {
    id("safer-library.conventions")
}

generateBuildInfo(
    "com.swiftleap.safer",
    BuildProp("compilerPluginName", project(":safer-compiler-plugin").name),
    BuildProp("kotlinVersion", libs.versions.kotlin.get())
)