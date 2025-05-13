package com.swiftleap.safer

import kotlin.test.Test

class UnusedElvisTests : AbstractTest() {
    @Test
    fun `NOT used in elvis operator`() {
        val unusedType = """
                    annotation class CheckReturnValue

                    @CheckReturnValue
                    class Foo
                    
                    fun returnsNullableValue(): Foo = Foo()
                    fun main() {
                        val value: Foo? = null
                        value ?: returnsNullableValue()
                    }
                    """.trimIndent()

        expectUnused(
            unusedCount = 1,
            sources = listOf(unusedType)
        )
    }

    @Test
    fun `USED in elvis operator`() {
        val fnUsedInElvis = """
                    annotation class CheckReturnValue

                    @CheckReturnValue
                    fun returnsNullableValue(): String? = "value"
                    
                    fun main() {
                        val value: String? = null
                        val used = value ?: returnsNullableValue()
                    }
                    """.trimIndent()

        val typeUsedInElvis = """
                    annotation class CheckReturnValue

                    @CheckReturnValue
                    class Foo
                    
                    fun returnsNullableValue(): Foo = Foo()
                    fun main() {
                        val value: Foo? = null
                        val used = value ?: returnsNullableValue()
                    }
                    """.trimIndent()

        //TODO unusedMethod gets lost in the elvis
        val unusedFn = """
                    annotation class CheckReturnValue

                    @CheckReturnValue
                    fun returnsNullableValue(): String? = "value"
                    fun main() {
                        val value: String? = null
                        value ?: returnsNullableValue()
                    }
                    """.trimIndent()

        expectUnused(
            unusedCount = 0,
            sources = listOf(fnUsedInElvis, typeUsedInElvis, unusedFn)
        )
    }
}