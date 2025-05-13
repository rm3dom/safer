package com.swiftleap.safer

import kotlin.test.Test

// These tests are not methodical.
class UnusedOtherTests : AbstractTest() {

    @Test
    fun `kotlin result additional check type`() {
        val pureContractTest = """
                    annotation class Contract(val pure: Boolean = false)
                    
                    @Contract(pure = true)
                    fun returnsTrue() = true

                    fun main() {
                        returnsTrue()
                    }
                    """.trimIndent()

        val resultTest = """
                    fun returnsTrue() : Result<Boolean> = Result.success(true)
                    fun main() {
                        returnsTrue()
                    }
                    """.trimIndent()

        expectUnused(
            unusedCount = 2,
            sources = listOf(pureContractTest, resultTest)
        )
    }

    @Test
    fun `used in destructuring declaration`() {
        expectUnusedSingle(
            unusedCount = 0,
            source = """
                    annotation class CheckReturnValue

                    data class Pair(val first: String, val second: String)

                    @CheckReturnValue
                    fun returnsPair(): Pair = Pair("first", "second")

                    fun main() {
                        val (first, second) = returnsPair()
                    }
                    """.trimIndent()
        )
    }
}
