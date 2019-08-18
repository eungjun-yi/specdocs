package im.toss.test.specdocs

import im.toss.test.equalsTo
import im.toss.test.shouldBeEmpty
import org.junit.jupiter.api.Test

internal class AbstractSpecFromTestReporterTest {

    @Test
    fun `Implement AbstractSpecFromTestReporterTest`() {
        val reporter = object: AbstractSpecFromTestReporter() {}

        reporter.basePaths().shouldBeEmpty()
        reporter.specFilter().equalsTo(SpecFilter())
        reporter.baseUri().equalsTo("")
    }
}
