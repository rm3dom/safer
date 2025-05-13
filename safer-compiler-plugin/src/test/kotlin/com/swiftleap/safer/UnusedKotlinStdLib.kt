package com.swiftleap.safer

import kotlin.test.Test

class UnusedKotlinStdLib : AbstractTest() {
    @Test
    fun `unsued function in stdlib`() {
        val unusedList = listOf(
            "\"a\".uppercase()",
            "\"a\".uppercase()",
            "\"a\".replace(\"a\", \"b\")",
            "listOf(1, 2, 3).filter { it > 1 }",
            "arrayOf(1, 2, 3).toList()",
            "mapOf(1 to \"one\").mapValues { it.value.uppercase() }"
        ).joinToString("\n")

        expectUnusedSingle(
            unusedCount = 6,
            source = """
                fun main() {
                    $unusedList
                }
                """.trimIndent()
        )
    }
}