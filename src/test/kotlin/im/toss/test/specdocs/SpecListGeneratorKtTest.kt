package im.toss.test.specdocs

import im.toss.test.equalsTo
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.provider.Arguments

internal class SpecListGeneratorKtTest {

    data class Parameter(
        override val fixture: Any? = null,
        override val given: Any = "a",
        override val expected: Any = "A"
    ) : SpecParameter

    @TestFactory
    fun toTestParameter(): List<DynamicTest> = listOf(
        null to null,
        listOf(Arguments.of("")) to null,
        listOf(Arguments.of(Parameter())) to TestParameter(null, listOf("a" to "A"))
    ).map { (given, expected) ->
        dynamicTest("$given then $expected") {
            given.toTestParameter().equalsTo(expected)
        }
    }

    @TestFactory
    fun toTable(): List<DynamicTest> {

        val precondition = "okay"

        return listOf(
            listOf(
                Arguments.of("a", "A")
            ) to listOf("| String | String |", "| ------ | ------ |", "| a | A |"),

            listOf(
                Arguments.of(Parameter())
            ) to listOf("| If | Then |", "| ---- | ---- |", "| a | A |"),

            listOf(
                Arguments.of(Parameter(fixture = precondition))
            ) to listOf(precondition, "\n", "| If | Then |", "| ---- | ---- |", "| a | A |")
        ).map { (given, expected) ->
            dynamicTest("$given then $expected") {
                given.toTable().equalsTo(expected)
            }
        }
    }

    @Test
    fun emptyTableOfExamples() {
        tableOfExamples(
            object: SpecDescriptor {
                override fun description(precondition: Any?, table: String?) = ""
            }, TestParameter(
                fixture = null,
                examples = emptyList()
            )
        ).equalsTo(
            listOf(
                Line(LineType.TEXT, "")
            )
        )

        tableOfExamples(
            object: SpecDescriptor {
                override fun description(precondition: Any?, table: String?) = ""
            }, TestParameter(
                fixture = null,
                examples = emptyList()
            )
        ).equalsTo(
            listOf(
                Line(LineType.TEXT, "")
            )
        )
    }
}
