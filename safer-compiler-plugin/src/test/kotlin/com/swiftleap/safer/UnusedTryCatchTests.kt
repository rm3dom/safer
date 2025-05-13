package com.swiftleap.safer

import kotlin.test.Test

class UnusedTryCatchTests : AbstractTest() {
    @Test
    fun `USED in try catch`() {
        val typeUsedInTryExpression = """
                    annotation class CheckReturnValue

                    @CheckReturnValue
                    class Foo

                    fun returnsFoo() = Foo()
                    fun main() {
                        val used = try {
                            returnsFoo()
                        } catch (e: Exception) {
                            returnsFoo()
                        }
                    }
                    """.trimIndent()

        val usedInTryBlock = """
                    annotation class CheckReturnValue

                    @CheckReturnValue
                    class Foo

                    fun returnsTrue() = Foo()
                    fun main() {
                        try {
                            val used = returnsTrue()
                            true
                        } catch (e: Exception) {
                            false
                        }
                    }
                    """.trimIndent()

        val usedInCatchBlock = """
                    annotation class CheckReturnValue

                    @CheckReturnValue
                    class Foo

                    fun returnsTrue() = Foo()
                    fun main() {
                        val used = try {
                            throw Exception()
                        } catch (e: Exception) {
                            returnsTrue()
                        }
                    }
                    """.trimIndent()

        val usedInTryWhenExpression = """
                    annotation class CheckReturnValue

                    @CheckReturnValue
                    class Foo

                    fun returnsFoo() = Foo()
                    fun main() = try {
                            when (val i = 10) {
                                else -> returnsFoo() 
                            }
                        } catch (e: Exception) {
                            returnsFoo()
                        }
                    """.trimIndent()

        expectUnused(
            unusedCount = 0,
            sources = listOf(
                typeUsedInTryExpression,
                usedInTryBlock,
                usedInCatchBlock,
                usedInTryWhenExpression
            )
        )
    }

    @Test
    fun `NOT used in try catch`() {
        val typeNotUsedInTry = """
                    annotation class CheckReturnValue

                    @CheckReturnValue
                    class Foo
            
                    fun returnsTrue() = Foo()
                    fun main() {
                        try {
                            returnsTrue()
                        } catch (e: Exception) {
                            false
                        }
                    }
                    """.trimIndent()

        val typeNotUsedInCatch = """
                    annotation class CheckReturnValue

                    @CheckReturnValue
                    class Foo

                    fun returnsTrue() = Foo()
                    fun main() {
                        try {
                            false
                        } catch (e: Exception) {
                            returnsTrue()
                        }
                    }
                    """.trimIndent()


        //Counts as 3
        val typeNotUsedInWhen = """
                    annotation class CheckReturnValue

                    @CheckReturnValue
                    class Foo

                    fun returnsTrue() = Foo()
                    fun main() {
                        try {
                            returnsTrue()
                        } catch (e: Exception) {
                             returnsTrue()
                        }
                    }
                    """.trimIndent()

        expectUnused(
            unusedCount = 5,
            sources = listOf(typeNotUsedInTry, typeNotUsedInCatch, typeNotUsedInWhen)
        )
    }

}