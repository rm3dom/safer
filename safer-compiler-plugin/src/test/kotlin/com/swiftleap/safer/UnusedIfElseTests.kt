package com.swiftleap.safer

import kotlin.test.Test

class UnusedIfElseTests : AbstractTest() {


    @Test
    fun `NOT used in if statement`() {

        //Counts 1
        val fnNotUsed = """
                    annotation class CheckReturnValue

                    @CheckReturnValue
                    fun returnsTrue() = true
                    fun main() {
                        if (true) {
                            returnsTrue()
                        }
                    }
                    """.trimIndent()

        //Counts 1
        val fnNotUsedInElse = """
                    annotation class CheckReturnValue

                    @CheckReturnValue
                    fun returnsTrue() = true
                    fun main() {
                        if (true) {
                            false
                        } else {
                            returnsTrue()
                        }
                    }
                    """.trimIndent()

        //Counts 3
        val typeNotUsed = """
                    annotation class CheckReturnValue

                    @CheckReturnValue
                    class Foo
                    
                    fun returnsFoo() = Foo()
                    
                    fun main() {
                        if (true) {
                            val i = 0
                            returnsFoo()
                        } else {
                            returnsFoo()
                        }
                    }
                    """.trimIndent()

        //Counts 2
        val fnNotUsedInExpression = """
                    annotation class CheckReturnValue

                    class Foo

                    @CheckReturnValue
                    fun returnsFoo() = Foo()
                    
                    fun main() {
                        if (true) {
                            returnsFoo()
                        } else {
                            returnsFoo()
                        }
                    }
                    """.trimIndent()

        expectUnused(
            unusedCount = 7,
            sources = listOf(fnNotUsed, fnNotUsedInElse, typeNotUsed, fnNotUsedInExpression)
        )
    }

    @Test
    fun `USED in else statement`() {

        val usedInElse = """
                    annotation class CheckReturnValue

                    @CheckReturnValue
                    fun returnsTrue() = true
                    fun main() {
                        if (true) {
                            false
                        } else {
                            val used = returnsTrue()
                            true
                        }
                    }
                    """.trimIndent()

        val usedInIf = """
                    annotation class CheckReturnValue

                    @CheckReturnValue
                    fun returnsTrue() = true
                    fun main() {
                        if (true) {
                            val used = returnsTrue()
                        }
                    }
                    """.trimIndent()

        val fnUsedInExpression = """
                    annotation class CheckReturnValue

                    @CheckReturnValue
                    fun returnsTrue() = true
                    fun main() {
                        val used = if (true) {
                            returnsTrue()
                        } else {
                            false
                        }
                    }
                    """.trimIndent()

        val typeUsedInExpression = """
                    annotation class CheckReturnValue

                    @CheckReturnValue
                    class Foo
                    
                    fun returnsFoo() = Foo()
                    
                    fun main() {
                        val used = if (true) {
                            returnsFoo()
                        } else {
                            returnsFoo()
                        }
                    }
                    """.trimIndent()

        expectUnused(
            unusedCount = 0,
            sources = listOf(usedInElse, usedInIf, fnUsedInExpression, typeUsedInExpression)
        )
    }
}