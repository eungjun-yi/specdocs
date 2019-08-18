package im.toss.test.specdocs

import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.junit.platform.launcher.TestPlan
import java.nio.file.Files
import java.nio.file.Paths

abstract class AbstractSpecFromTestReporter: TestExecutionListener {
    private val treeBuilder: SpecDocumentationTreeBuilder =
        SpecDocumentationTreeBuilder(
            filter = specFilter(),
            basePaths = basePaths()
        )

    open fun basePaths(): List<String> = emptyList()

    open fun specFilter(): SpecFilter = SpecFilter()

    open fun baseUri(): String = ""

    open fun pathToSpecDocument() = "build/reports/specs/Specification.md"

    override fun executionFinished(
        testIdentifier: TestIdentifier,
        testExecutionResult: TestExecutionResult
    ) {
        treeBuilder.addNodes(listOf(testIdentifier.toTest()))
    }

    override fun testPlanExecutionFinished(testPlan: TestPlan) {
        val pathToSpecDocument = Paths.get(pathToSpecDocument())
        pathToSpecDocument.parent.toFile().mkdirs()
        Files.write(
            pathToSpecDocument,
            SpecDocumentationGenerator(baseUri()).generate(treeBuilder.build()).toByteArray()
        )
    }
}

internal fun TestIdentifier.toTest(): TestItem {
    return TestItem(
        this.uniqueId,
        this.displayName.removeSuffix("()")
    )
}
