package com.swiftleap.safer

import kotlin.test.Test

class UnusedFunctionTests : AbstractTest() {
    @Test
    fun `NOT used in function`() {
        val fnNotUsed = """
                    annotation class CheckReturnValue

                    @CheckReturnValue
                    fun returnsTrue() = true
                    fun main() { 
                        returnsTrue(); 
                    }
                    """.trimIndent()

        val typeNotUsed = """
                    annotation class CheckReturnValue

                    @CheckReturnValue
                    class Foo
                    
                    fun returnsFoo() = Foo()
                    fun returnsBar() = returnsFoo()
                    fun main() {
                        returnsBar()
                    }
                    """.trimIndent()

        val typeNotUsedInBuilder = """
                    annotation class CheckReturnValue

                    @CheckReturnValue
                    class Builder {
                        fun addItem(item: String): Builder = this
                        fun build(): String = "result"
                    }

                    fun main() {
                        val builder = Builder()
                        builder.addItem("item")
                        val result = builder.build()
                    }
                    """

        expectUnused(
            unusedCount = 3,
            sources = listOf(fnNotUsed, typeNotUsed, typeNotUsedInBuilder)
        )
    }

    @Test
    fun `USED in function`() {

        val notChecked = """
                    fun returnsTrue() = true
                    fun main() { 
                        returnsTrue()
                    }
                    """.trimIndent()

        val fnUsed = """
                    annotation class CheckReturnValue

                    @CheckReturnValue
                    fun returnsTrue() = true
                    fun main() {
                        val unused = returnsTrue()
                    }
                    """.trimIndent()

        val chainingUsed = """
                    annotation class CheckReturnValue

                    @CheckReturnValue
                    class ChainableClass {
                        fun method1(): ChainableClass = this
                        fun method2(): ChainableClass = this
                        fun finalMethod(): String = "result"
                    }

                    fun getChainable(): ChainableClass = ChainableClass()

                    fun main() {
                        val result = getChainable().method1().method2().finalMethod()
                    }
                    """.trimIndent()

        val nestedUsed = """
                    annotation class CheckReturnValue

                    @CheckReturnValue
                    fun returnsString(): String = "value"

                    fun takesString(s: String): Boolean = true

                    fun main() {
                        val result = takesString(returnsString())
                    }
                    """.trimIndent()

        expectUnused(
            unusedCount = 0,
            sources = listOf(notChecked, fnUsed, chainingUsed, nestedUsed)
        )
    }
}