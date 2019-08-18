package im.toss.test.specdocs.examples

import im.toss.test.specdocs.AbstractSpecFromTestReporter
import im.toss.test.specdocs.SpecFilter

class SpecFromTestReporter: AbstractSpecFromTestReporter() {
    override fun baseUri() = ""

    override fun basePaths(): List<String> {
        return listOf(
            "im.toss.test.specdocs"
        )
    }

    override fun specFilter(): SpecFilter {
        return SpecFilter(
            includes = listOf(
                "im.toss.test.specdocs.examples"
            )
        )
    }
}
