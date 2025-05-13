package com.swiftleap.safer

import kotlin.test.Test

class UnusedLambdaTests : AbstractTest() {
    @Test
    fun `NOT used in lambda`() {
        expectUnused(
            unusedCount = 1,
            sources = listOf(
                """
                    annotation class Pure

                    @Pure
                    class Foo

                    fun returnsFoo() = Foo()
                    fun main() {
                        val lambda = { returnsFoo(); Unit }
                        lambda()
                    }
                    """.trimIndent()
            )
        )
    }

    @Test
    fun `used in lambda`() {

        val fnUsedInAnonymous = """
                    annotation class CheckReturnValue

                    class Foo
                    
                    @CheckReturnValue
                    fun Foo.returnsTrue(any: Foo) = true

                    fun main() {
                        val used = listOf(Foo()).zip(listOf(Foo())).all { (l, r) -> l.returnsTrue(r) }
                    }
                    """.trimIndent()

        val fnUsedInLambda = """
                    annotation class CheckReturnValue

                    @CheckReturnValue
                    fun returnsTrue() = true
                    
                    fun main() {
                        val lambda = { val unused = returnsTrue(); Unit }
                        lambda();
                    }
                    """.trimIndent()

        val typeUsedInLambda = """
                    annotation class CheckReturnValue

                    @CheckReturnValue
                    class Foo
                    
                    fun returnsFoo() = Foo()
                    
                    fun main() {
                        val lambda = { returnsFoo() }
                        val used = lambda();
                    }
                    """.trimIndent()

        expectUnused(
            unusedCount = 0,
            sources = listOf(fnUsedInAnonymous, fnUsedInLambda, typeUsedInLambda)
        )
    }
}