package com.swiftleap.safer

import com.swiftleap.safer.plugin.*
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.Services
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.Path
import kotlin.test.assertEquals


abstract class AbstractTest {
    private object Compiler {
        val compiler = K2JVMCompiler()
        val testCounter = AtomicInteger(0)
    }

    protected fun compileKotlin(options: Array<String>, sources: List<String>) {
        val testCounter = Compiler.testCounter.incrementAndGet()
        val classPath = Paths.get(".")
            .resolve("build/libs/${BuildInfo.compilerPluginName}-${BuildInfo.projectVersion}.jar")
            .toAbsolutePath()
            .toFile()

        if (classPath.exists().not())
            throw Exception("Compiler plugin jar not found, assemble first")

        //Output dir
        val dir = Path("/tmp/test")
        val outputDir = dir.toFile()
        if (!outputDir.exists() && !outputDir.mkdirs())
            error("Failed to create output dir: $outputDir")

        //Source file
        val sourceList = sources.mapIndexed { i, source ->
            val testName = "test${testCounter}$i"
            val sourceFile = dir.resolve("$testName.kt").toFile()
            sourceFile.writeText("package $testName\n\n$source")
            sourceFile.path
        }


        //Compile
        val arguments = K2JVMCompilerArguments().apply {
            freeArgs = sourceList
            verbose = true
            destination = outputDir.path
            pluginClasspaths = arrayOf(classPath.path)
            pluginOptions = options
            classpath = System.getProperty("java.class.path")
        }
        val renderer = MessageRenderer.PLAIN_RELATIVE_PATHS
        val messageCollector: MessageCollector = PrintingMessageCollector(System.err, renderer, true)
        val exit = Compiler.compiler.exec(messageCollector, Services.EMPTY, arguments)
        System.gc()
        when (exit) {
            ExitCode.OK -> Unit
            else -> throw Exception("Compilation failed with exit code: $exit")
        }
    }

    protected fun expectUnusedSingle(unusedCount: Int, source: String) = expectUnused(unusedCount, listOf(source))

    protected fun expectUnused(unusedCount: Int, sources: List<String>) {
        val unused = mutableListOf<TestEvent.ResultNotUsed>()
        TestHooks.register {
            when (it) {
                is TestEvent.ResultNotUsed -> unused.add(it)
                else -> Unit
            }
        }

        compileKotlin(
            arrayOf(
                PluginConfiguration::unsafeEnabled.asPluginCliOption(false),
                PluginConfiguration::unusedEnabled.asPluginCliOption(true),
                PluginConfiguration::unusedWarnAsError.asPluginCliOption(false),
                PluginConfiguration::unusedPresetLibs.asPluginCliOption(setOf("kotlin-stdlib", "java")),
                PluginConfiguration::unusedSignatures.asPluginCliOption(
                    setOf(
                        "*.@Pure"
                    )
                )
            ), sources
        )

        assertEquals(
            unusedCount,
            unused.size,
            "Unexpected unused count"
        )
    }

    protected fun expectUnsafeSingle(unsafeCount: Int, source: String) = expectUnsafe(unsafeCount, listOf(source))

    protected fun expectUnsafe(unsafeCount: Int, sources: List<String>) {
        val unsafe = mutableListOf<TestEvent.UnsafeFunction>()
        TestHooks.register {
            when (it) {
                is TestEvent.UnsafeFunction -> unsafe.add(it)
                else -> Unit
            }
        }

        compileKotlin(
            arrayOf(
                PluginConfiguration::unsafeEnabled.asPluginCliOption(true),
                PluginConfiguration::unsafePresetLibs.asPluginCliOption(setOf("kotlin-stdlib", "java")),
                PluginConfiguration::unsafeWarnAsError.asPluginCliOption(false),
                PluginConfiguration::unusedEnabled.asPluginCliOption(false),
            ), sources
        )

        assertEquals(
            unsafeCount,
            unsafe.size,
            "Unexpected unsafe count"
        )
    }
}
