package test

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class CheckReturnValue

@CheckReturnValue
fun testCheckReturn() = true

fun main() {
    testCheckReturn()
}