package com.swiftleap.safer

import kotlin.test.Test

class UnsafeTests : AbstractTest() {
    @Test
    fun `unsafe function`() {
        val usafeList = listOf(
            // Original unsafe functions from the test
            "println(listOf(10).first())",
            "listOf(true)[2]",
            "\"\".toBoolean()",
            "\"\".toInt()",
            "arrayOf(true).get(1)",
            "arrayOf(true)[1]",
            "mutableListOf(true)[0]",
            "listOf(true).first()",
            "listOf(true).first { it == true }",
            "listOf(true).last()",
            "listOf(true).last { it == true }",
            "listOf(true).random()",
            "listOf(true).max()",
            "listOf(true).min()",
            "listOf(true).maxBy { it ==true }",
            "listOf(true).minBy { it ==true }",
            "\"\".first()",
            "\"\".single()",
            "\"\".maxOf { it }",

            // Additional unsafe functions from unsafe-kotlin-stdlib.txt
            "sequenceOf(1).first()",
            "sequenceOf(1).first { it > 0 }",
            "sequenceOf(1).last()",
            "sequenceOf(1).last { it > 0 }",
            "sequenceOf(1).max()",
            "sequenceOf(1).min()",
            "sequenceOf(1).maxBy { it }",
            "sequenceOf(1).minBy { it }",
            "sequenceOf(1).maxOf { it }",
            "sequenceOf(1).maxOfWith(compareBy { it }, { it })",
            "\"\".elementAt(0)",
            "\"\".last()",
            "\"\".random()",
            "\"\".max()",
            "\"\".min()",
            "\"\".maxBy { it }",
            "\"\".minBy { it }",
            "\"\".maxOfWith(compareBy { it }, { it })",
            "\"\".toBigDecimal()",
            "\"\".toBigInteger()",
            "\"\".toByte()",
            "\"\".toDouble()",
            "\"\".toFloat()",
            "\"\".toShort()",
            "\"\".toUByte()",
            "\"\".toULong()",
            "\"\".toUInt()",
            "\"\".toUShort()",

            // Additional unsafe functions from unsafe-java.txt
            "java.util.Optional.empty<Int>().get()",
            "java.util.ArrayList<Int>().get(0)",
            "java.util.LinkedList<Int>().get(0)",
            "java.util.Vector<Int>().get(0)",
            "java.util.HashMap<String, Int>().get(\"key\")",
            "java.util.TreeMap<String, Int>().get(\"key\")",
            "java.util.LinkedHashMap<String, Int>().get(\"key\")",
            "java.util.Hashtable<String, Int>().get(\"key\")"
        )

        val all = usafeList.joinToString("\n")

        expectUnsafeSingle(
            unsafeCount = usafeList.size,
            source = """
                    fun main() {
                        $all
                    }
                    """.trimIndent()
        )
    }

    @Test
    fun `safe function in stdlib`() {
        //These are safe because boxing/lifting is slow (TODO add boundary check analysis, some stuff is obviously unsafe)
        val safeList = listOf(
            "\"\"[0]",
            "\"\".get(0)",
            "IntArray(0)[0]",
            "ByteArray(0)[0]",
            "CharArray(0)[0]",
            "ShortArray(0)[0]",
            "LongArray(0)[0]",
            "UByteArray(0)[0]",
            "BooleanArray(0)[0]",
        )

        val all = safeList.joinToString("\n")
        expectUnsafeSingle(
            unsafeCount = 0,
            source = """
                    fun main() {
                        $all
                    }
                    """.trimIndent()
        )
    }
}
