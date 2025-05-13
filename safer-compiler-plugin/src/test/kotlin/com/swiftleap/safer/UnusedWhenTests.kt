package com.swiftleap.safer

import kotlin.test.Test

class UnusedWhenTests : AbstractTest() {
    @Test
    fun `NOT used in when`() {
        val typeNotUsedInExpression = """
                    annotation class CheckReturnValue

                    @CheckReturnValue
                    class Foo

                    fun returnsFoo() = Foo()
                    fun main() {
                        when (val i = 1) {
                            else -> returnsFoo()
                        }
                    }
                    """.trimIndent()

        val typeNotUsedInBranch = """
                    annotation class CheckReturnValue
                    @CheckReturnValue
                    fun returnsTrue() = true
                    fun main() {
                        when (val i = 1) {
                            1 -> {
                                returnsTrue()
                                true
                            }
                            else -> false
                        }
                    }
                    """.trimIndent()

        expectUnused(
            unusedCount = 2,
            sources = listOf(typeNotUsedInExpression, typeNotUsedInBranch)
        )

    }

    @Test
    fun `USED in when`() {
        val typeUsedInExpression = """
                    annotation class CheckReturnValue

                    @CheckReturnValue
                    class Foo

                    fun returnsFoo() = Foo()
                    fun main() {
                        val used = when (val i = 1) {
                            else -> returnsFoo()
                        }
                    }
                    """.trimIndent()

        val fnUsedInExpression = """
                    annotation class CheckReturnValue
                    @CheckReturnValue
                    fun returnsTrue() = true
                    
                    fun main() {
                        val used = when (val i = 1) {
                            1 -> {
                                returnsTrue()
                            }
                            else -> returnsTrue()
                        }
                    }
                    """.trimIndent()

        //TODO the method is used, however the result was not used. Not really a bug, but not really working either.
        val methodNotUsedInExpression = """
                    annotation class CheckReturnValue
                    @CheckReturnValue
                    fun returnsTrue() = true
                    
                    fun main() {
                        when (val i = 1) {
                            1 -> returnsTrue()
                            else -> returnsTrue()
                        }
                    }
                    """.trimIndent()

        val fnUsedInRecursiveExpression = """
                    fun returnWhen(i: Int, list: List<Int>) : List<Int> = 
                        when (i) {
                            0 -> {
                                val y = returnWhen(2, list)
                                list + y
                            }
                            else -> {
                                val y = returnWhen(2, list)
                                list + y
                            }
                        }
                    
                    fun main() {
                        val used = returnWhen(0, emptyList())
                    }
                    """.trimIndent()

        expectUnused(
            unusedCount = 0,
            sources = listOf(
                typeUsedInExpression,
                fnUsedInExpression,
                methodNotUsedInExpression,
                fnUsedInRecursiveExpression
            )
        )
    }
}