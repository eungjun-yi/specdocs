package im.toss.test.specdocs

import org.junit.jupiter.params.provider.Arguments

class SpecListGenerator(
    private val testcodeBaseUri: String
) {

    fun generate(root: Node) = generate(root, 1)

    private fun generate(node: Node, depth: Int): String {
        val lines = generateDocumentLines(node, depth)
            .fold(listOf<Line>()) { lines, line ->
                if (lines.lastOrNull()?.type == LineType.NEWLINE && line.type == LineType.NEWLINE) {
                    lines
                } else {
                    lines + line
                }
            }.mapNotNull {
                when (it.type) {
                    LineType.TEXT -> it.text
                    LineType.NEWLINE -> ""
                    LineType.NONE -> null
                }
            }

        return lines.joinToString("\n").trim()
    }

    private fun generateDocumentLines(node: Node, depth: Int): List<Line> {
        val children = node.children
        val hasNoChildren = children.isEmpty()
        val titleText = node.title ?: node.id
        val source = node.source
        val title = title(source, titleText)
        val description = source?.description
        val descriptor = source?.descriptor
        val arguments = source?.arguments
        val nextDepth = nextDepth(titleText, depth)
        val isParameterizedTest = arguments != null

        return documentSelf(
            titleText, hasNoChildren, title, depth, descriptor, arguments, description
        ) + if (isParameterizedTest) {
            // arguments가 있다면 parameterized test로 간주하고
            // documentSelf에서 parameter table을 만들었을테니 children을 문서화할 필요는 없다고 간주한다.
            // FIXME: 그렇다기보다 그것들을 children이라고 봐야??
            emptyList()
        } else {
            documentChildren(children, nextDepth)
        }
    }

    private fun title(
        source: TestSource?,
        titleText: String?
    ) = if (source != null) {
        val lineNumberString = if (source.line != null) {
            "#L${source.line}"
        } else {
            ""
        }
        val uri = "$testcodeBaseUri/${source.filepath}$lineNumberString"
        "[$titleText]($uri)"
    } else {
        titleText
    }

    private fun nextDepth(titleText: String?, depth: Int): Int {
        return if (titleText != null) {
            depth + 1
        } else {
            depth
        }
    }

    private fun documentChildren(
        children: List<Node>,
        nextDepth: Int
    ): List<Line> {
        return children
            .sortedBy { it.children.isNotEmpty() }
            .flatMap { generateDocumentLines(it, nextDepth) }
    }

    private fun documentSelf(
        titleText: String?,
        hasNoChildren: Boolean,
        title: String?,
        depth: Int,
        descriptor: SpecDescriptor?,
        arguments: List<Arguments>?,
        description: String?
    ) = when {
        titleText == null -> listOf(Line(LineType.NONE))
        hasNoChildren -> listItem(title)
        else -> section(depth, title, descriptor, arguments, description)
    }

    private fun section(
        depth: Int,
        titleWithLink: String?,
        descriptor: SpecDescriptor?,
        arguments: List<Arguments>?,
        description: String?
    ) = listOf(
        Line(LineType.NEWLINE),
        title(depth, titleWithLink),
        Line(LineType.NEWLINE)
    ) + if (descriptor != null) {
        lines(arguments.toTestParameter()) { tableOfExamples(descriptor, it) }
    } else {
        lines(description) { description(it) } + lines(arguments) { tableOfArguments(it) }
    }

    private fun title(depth: Int, titleWithLink: String?) = Line(LineType.TEXT, "#".repeat(depth) + " $titleWithLink")

    private fun <T> lines(value: T?, func: (T) -> List<Line>): List<Line> = if (value != null) func.invoke(value) else emptyList()

    private fun description(description: String): List<Line> {
        return listOf(
            Line(LineType.TEXT, description),
            Line(LineType.NEWLINE)
        )
    }

    private fun tableOfArguments(arguments: List<Arguments>): List<Line> {
        return arguments.toTable().map {
            Line(LineType.TEXT, it)
        } + listOf(Line(LineType.NEWLINE))
    }

    private fun listItem(titleWithLink: String?): List<Line> {
        return listOf(
            Line(LineType.TEXT, "* $titleWithLink")
        )
    }
}

data class TestParameter(
    val fixture: Any?,
    val examples: List<Pair<Any, Any>>
)

internal fun List<Arguments>?.toTestParameter(): TestParameter? {
    if (this == null) {
        return null
    }

    val firstArguments = this.first()
    if (firstArguments.get().size != 1) {
        return null
    }

    if (firstArguments.get().first() !is SpecParameter) {
        return null
    }

    val specParameter = firstArguments.get().first() as SpecParameter
    val fixture = specParameter.fixture

    val examples = this.map {
        val params = it.get().first() as SpecParameter
        params.given to params.expected
    }

    return TestParameter(fixture, examples)
}

internal fun List<Pair<Any, Any>>.toExampleTable(): List<String> {
    val tableHeader = "| If | Then |"
    val tableSplitter = "| ---- | ---- |"
    val tableContents = this.map {
        val (given, expected) = it
        "| ${listOf(given, expected).joinToString(" | ")} |"
    }
    return listOf(tableHeader, tableSplitter) + tableContents
}

internal fun List<Arguments>.toTable(): List<String> {
    val firstArguments = this.first()
    val testParameter = this.toTestParameter()
    return if (testParameter != null) {
        val precondition = testParameter.fixture
        val exampleTable = testParameter.examples.toExampleTable()
        if (precondition != null) {
            listOf(precondition.toString(), "\n")
        } else {
            emptyList()
        } + exampleTable
    } else {
        val columns = firstArguments.get().map { it.javaClass.simpleName }
        val tableHeader = "| ${columns.joinToString(" | ")} |"
        val tableSplitter = "| ${columns.map { "-".repeat(it.length) }.joinToString(" | ")} |"
        val tableContents = this.map { "| ${it.get().joinToString(" | ")} |"  }
        listOf(tableHeader, tableSplitter) + tableContents
    }
}

internal fun tableOfExamples(
    descriptor: SpecDescriptor,
    testParam: TestParameter
): List<Line> {
    return descriptor.description(
        testParam.fixture,
        testParam.examples.toExampleTable().joinToString("\n")
    ).split("\n").map {
        Line(LineType.TEXT, it)
    }
}

internal enum class LineType {
    TEXT, NEWLINE, NONE
}

internal data class Line(
    val type: LineType,
    val text: String = ""
)
