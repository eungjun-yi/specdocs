package im.toss.test.specdocs.examples

import im.toss.test.equalsTo
import im.toss.test.specdocs.SpecDescription
import im.toss.test.specdocs.SpecDescriptor
import im.toss.test.specdocs.SpecGeneration
import im.toss.test.specdocs.SpecParameter
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class SpecGenerationTest {

    @ParameterizedTest
    @MethodSource("parameterSource")
    @SpecGeneration(MySpec::class)
    fun test(params: Parameter) {
        val solver = Solver()
        solver.solve(params.given).equalsTo(params.expected)
    }

    @ParameterizedTest
    @MethodSource("parameterSource")
    @SpecDescription("Capitalize the given string.")
    fun test2(params: Parameter) {
        val solver = Solver()
        solver.solve(params.given).equalsTo(params.expected)
    }

    companion object {
        data class Parameter(
            override val fixture: String? = null,
            override val given: String,
            override val expected: String
        ) : SpecParameter

        @JvmStatic
        fun parameterSource() = listOf(
            Arguments.of(Parameter(given = "a", expected = "A")),
            Arguments.of(Parameter(given = "b", expected = "B"))
        )
    }
}

class MySpec: SpecDescriptor {
    override fun description(precondition: Any?, table: String?) = """
Capitalize the given string.

$table
""".trimIndent()
}
