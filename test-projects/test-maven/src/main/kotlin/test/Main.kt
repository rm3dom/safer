package test

import java.util.Optional

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class CheckReturnValue

@CheckReturnValue
fun testCheckReturn() = true

fun javaOptional() = Optional.of(1)

fun main() {
    javaOptional()
    testCheckReturn()
    listOf(1)
}