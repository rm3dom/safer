plugins {
    kotlin("jvm")
    `java-library`
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

val buildTool = stringProperty("safer.buildTool", "")

dependencies {
    if (buildTool == "maven")
        compileOnly(libs.kotlin.compiler)
    else
        compileOnly(libs.kotlin.compiler.embeddable)

    testImplementation(libs.kotlin.compiler.embeddable)
    testImplementation(libs.kotlin.test)
}

tasks.create("rewrite-types") {
    //Pairs of "kotlin-compiler" to "kotlin-compiler-embeddable" strings
    val pairs = listOf(
        "import com.intellij.psi.PsiElement" to "import org.jetbrains.kotlin.com.intellij.psi.PsiElement"
    )
    doFirst {
        println("Rewriting kotlin-compiler types for $buildTool build...")

        project.file("src/main/kotlin/com/swiftleap/safer/plugin/checkers").walk().forEach { file ->
            if (file.isFile && file.extension == "kt") {
                println("   Rewriting ${file.name}...")
                if (buildTool == "maven") {
                    file.writeText(file.readText().let {
                        pairs.fold(it) { str, pair -> str.replace(pair.second, pair.first) }
                    })
                } else {
                    file.writeText(file.readText().let {
                        pairs.fold(it) { str, pair -> str.replace(pair.first, pair.second) }
                    })
                }
            }
        }
    }
}


tasks.compileKotlin {
    dependsOn("rewrite-types")
}

// Safer compiler plugin must be published with kotlin-compiler-embeddable and NOT kotlin-compiler
val validatePublish = tasks.create("validate-publish") {
    doFirst {
        if(buildTool != "gradle")
            error("""
                #################################################
                Publish disabled, use -P "safer.buildTool=gradle"
                #################################################
            """.trimIndent())
    }
}

tasks.publish {dependsOn(validatePublish)}

publishing {
    publications {
        named<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}