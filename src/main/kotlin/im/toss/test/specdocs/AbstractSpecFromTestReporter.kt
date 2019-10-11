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
        val root = treeBuilder.build()
        val baseUri = baseUri()
        Files.write(
            pathToSpecDocument,
            render(baseUri, root).toByteArray()
        )
    }

    open fun render(baseUri: String, root: Node) = DefaultSpecDocumentationGenerator(baseUri).generate(root)
}

internal fun TestIdentifier.toTest(): TestItem {
    return TestItem(
        this.uniqueId,
        this.displayName.removeSuffix("()")
    )
}

fun Node.findById(id: String): Node? {
    return if (this.id == id) {
        this
    } else {
        this.children.mapNotNull { it.findById(id) }.firstOrNull()
    }
}

