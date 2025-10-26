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
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.Path
import kotlin.test.assertEquals


abstract class AbstractTest {

    private object Compiler {
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
        val exit = K2JVMCompiler().exec(messageCollector, Services.EMPTY, arguments)
        System.gc()
        when (exit) {
            ExitCode.OK -> Unit
            else -> throw Exception("Compilation failed with exit code: $exit")
        }
    }

    protected fun expectUnsafeSingle(unsafeCount: Int, source: String) = expectUnsafe(unsafeCount, listOf(source))

    protected fun expectUnsafe(unsafeCount: Int, sources: List<String>) {
        val unsafe = mutableListOf<TestEvent.UnsafeFunction>()
        val _ = TestHooks.register {
            when (it) {
                is TestEvent.UnsafeFunction -> unsafe.add(it)
                else -> Unit
            }
        }

        compileKotlin(
            arrayOf(
                PluginConfiguration::unsafeEnabled.asPluginCliOption(true),
                PluginConfiguration::unsafePresetLibs.asPluginCliOption(setOf("kotlin-stdlib", "java")),
                PluginConfiguration::unsafeWarnAsError.asPluginCliOption(false)
            ), sources
        )

        assertEquals(
            unsafeCount,
            unsafe.size,
            "Unexpected unsafe count"
        )
    }
}
