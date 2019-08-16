package im.toss.test.specdocs

import im.toss.test.equalsTo
import org.junit.jupiter.api.Test

internal class TestSourceTest {

    @Test
    fun compareTo() {
        val testSource = TestSource(
            filepath = "b",
            line = 1,
            type = TestSource.Type.METHOD
        )

        testSource.compareTo(testSource.copy(line = 0)).equalsTo(1)
        testSource.compareTo(testSource.copy(line = 1)).equalsTo(0)
        testSource.compareTo(testSource.copy(line = 2)).equalsTo(-1)

        testSource.compareTo(testSource.copy(filepath = "a")).equalsTo(1)
        testSource.compareTo(testSource.copy(filepath = "b")).equalsTo(0)
        testSource.compareTo(testSource.copy(filepath = "c")).equalsTo(-1)

        testSource.compareTo(testSource.copy(line = null)).equalsTo(1)
        testSource.copy(line = null).compareTo(testSource.copy(line = null)).equalsTo(-1)
    }
}
