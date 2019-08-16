package im.toss.test.specdocs.examples

import im.toss.test.equalsTo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class Solver {
    fun solve(a: String) = a.toUpperCase()
}

/*
uniqueId = "[engine:junit-jupiter]/[class:im.toss.test.specter.examples.ArgumentTest]"
displayName = "ArgumentTest"
 */
class ArgumentTest {

/*
uniqueId = "[engine:junit-jupiter]/[class:im.toss.test.specter.examples.ArgumentTest]/[test-template:test(java.lang.String, java.lang.String)]"
displayName = "test(String, String)"
 */
    @ParameterizedTest
    @MethodSource("parameterSource")
    fun test(given: String, expected: String) {
        val solver = Solver()
        solver.solve(given).equalsTo(expected)
        solver.solve(given).equalsTo(expected)
    }

    companion object {
        @JvmStatic
        fun parameterSource() = listOf(
            /*
uniqueId = "[engine:junit-jupiter]/[class:im.toss.test.specter.examples.ArgumentTest]/[test-template:test(java.lang.String, java.lang.String)]/[test-template-invocation:#1]"
displayName = "[1] a, A"
             */
            Arguments.of("a", "A"),
            /*
uniqueId = "[engine:junit-jupiter]/[class:im.toss.test.specter.examples.ArgumentTest]/[test-template:test(java.lang.String, java.lang.String)]/[test-template-invocation:#2]"
displayName = "[2] b, B"
             */
            Arguments.of("b", "B")
        )
    }
}
