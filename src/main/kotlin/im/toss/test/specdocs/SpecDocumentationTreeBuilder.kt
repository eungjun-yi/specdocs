package im.toss.test.specdocs

import javassist.ClassPool
import javassist.CtClass
import javassist.NotFoundException
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.full.createInstance

class SpecDocumentationTreeBuilder(
    private val filter: SpecFilter = SpecFilter(),
    private val basePaths: List<String> = listOf()
) {

    private val nodes: MutableList<TestItem> = mutableListOf()

    fun addNodes(testItems: List<TestItem>) {
        this.nodes.addAll(testItems)
    }

    fun build(): Node {
        return this.merge(
            this.nodes.asSequence().map {
                it.toTestDescriptor()
            }.filterNot {
                // Remove unnecessary items like JUnit Vintage, ...
                it.path == listOf("")
            }.filter {
                val path = it.path
                if (filter.includes != null) {
                    filter.includes.any { path.startsWith(it.split(".")) }
                } else {
                    true
                }
            }.map {
                it.copy(
                    path = it.path.basePathRemoved()
                )
            }.toList()
        ).sorted()
    }

    fun merge(items: List<TestDescriptor>) = items.fold(Node()) { root, item ->
        merge(root, item)
    }

    private fun List<String>.basePathRemoved() = this.basePathRemoved(basePaths)

    companion object {
        fun merge(node: Node, item: TestDescriptor) = when (item.path.size) {
            0 -> node
            1 -> node.withChild(item)
            else -> node.withDescendant(item)
        }

        private fun Node.withDescendant(item: TestDescriptor): Node {
            val head = item.path.first()
            val tail = item.path.drop(1)
            val old = this.children.firstOrNull { it.id == head }
            val testDescriptor = TestDescriptor(tail, item.title, item.source)
            val newChildren = if (old != null) {
                val new = merge(old, testDescriptor)
                this.children.replaced(old, new)
            } else {
                val new = merge(
                    Node(head),
                    testDescriptor
                )
                this.children + new
            }

            return this.copy(
                children = newChildren
            )
        }

        private fun <T> List<T>.replaced(old: T, new: T) = this.map {
            if (it == old) {
                new
            } else {
                it
            }
        }

        private fun Node.withChild(item: TestDescriptor): Node {
            return this.copy(
                children = this.children.withSibling(item.path.firstOrNull(), item.title, item.source)
            )
        }

        private fun List<Node>.withSibling(id: String?, title: String, source: TestSource?) =
            if (this.map { it.id }.contains(id)) {
                this.replaced({
                    it.id == id
                }, {
                    it.copy(title = title, source = source)
                })
            } else {
                this + Node(
                    id = id,
                    title = title,
                    source = source
                )
            }

        private fun <T> List<T>.replaced(
            condition: (T) -> Boolean,
            new: (T) -> T
        ) = this.map {
            if (condition.invoke(it)) {
                new.invoke(it)
            } else {
                it
            }
        }
    }
}

data class SpecFilter(
    val includes: List<String>? = null
)

data class Node(
    val id: String? = null,
    val title: String? = null,
    val source: TestSource? = null,
    val children: List<Node> = emptyList()
) {
    fun sorted(): Node = this.copy(
        children = children
            .map { it.sorted() }
            .sortedBy { it.source }
    )
}

data class TestItem(
    val uniqueId: String,
    val displayName: String
)

fun TestItem.toTestDescriptor() = TestDescriptor(
    path = path(this.uniqueId),
    title = this.displayName.parametersRemoved(),
    source = source(this.uniqueId)
)

fun source(testIdString: String): TestSource? {
    val testId = testId(testIdString)

    val fullQualifiedName = java.net.URLDecoder.decode(testId.className ?: "", "UTF-8")
    val methodName = java.net.URLDecoder.decode(testId.methodName() ?: "", "UTF-8")
    val dynamicContainer = java.net.URLDecoder.decode(testId.dynamicContainer ?: "", "UTF-8")
    val dynamicTest = java.net.URLDecoder.decode(testId.dynamicTest ?: "", "UTF-8")

    if (fullQualifiedName.isBlank()) {
        return null
    }

    // We can't get the exact line of the test if it is a dynamic test.
    if (dynamicTest.isNotBlank()) {
        return null
    }

    // We can't get the exact line of the test if it is a dynamic container.
    if (dynamicContainer.isNotBlank()) {
        return null
    }

    return try {
        val classPool = ClassPool.getDefault()
        val ctClass = classPool.get(fullQualifiedName)
        if (methodName.isNotEmpty()) {
            methodSource(methodName, ctClass)
        } else {
            classSource(ctClass)
        }
    } catch (e: NotFoundException) {
        null
    }
}

private fun classSource(
    ctClass: CtClass
): TestSource {

    val description = ctClass.getAnnotation(SpecDescription::class.java) as SpecDescription?

    return TestSource(
        filepath = filepath(ctClass),
        type = TestSource.Type.CLASS,
        description = description?.value,
        fullQualifiedName = ctClass.name
    )
}

private fun filepath(ctClass: CtClass) =
    filepath(ctClass.name, ctClass.classFile.sourceFile)

private fun filepath(classPath: String, file: String?) =
    (classPath.split(".").let { it.subList(0, it.size - 1) } + file).joinToString("/")

private fun methodSource(
    methodName: String,
    ctClass: CtClass
): TestSource {

    // NOTE: 메서드가 overloading 된 경우 아무거나 하나를 얻는다.
    val replace = methodName.parametersRemoved()
    val method = ctClass.getDeclaredMethod(replace)
    val description = method.getAnnotation(SpecDescription::class.java) as SpecDescription?
    val specGeneration = method.getAnnotation(SpecGeneration::class.java) as SpecGeneration?
    val methodSource = method.getAnnotation(MethodSource::class.java) as MethodSource?
    val arguments = methodSource?.let {
        it.value.first().let { sourceMethodName ->
            Class.forName(ctClass.name).getMethod(sourceMethodName).invoke(null) as List<Arguments>
        }
    }

    val correctionOffset = -1 // To make this pos the method header not the body
    val line = method.methodInfo.getLineNumber(0) + correctionOffset

    val descriptor = specGeneration?.value?.createInstance()

    return TestSource(
        filepath = filepath(ctClass),
        type = TestSource.Type.METHOD,
        line = line,
        description = description?.value,
        descriptor = descriptor,
        arguments = arguments,
        fullQualifiedName = ctClass.name
    )
}

data class TestId(
    val className: String? = null,
    val nestedClasses: List<String>,
    private val method: String? = null,
    private val testFactory: String? = null,
    private val testTemplate: String? = null,
    val dynamicContainer: String? = null,
    val dynamicTest: String? = null,
    val testTemplateInvocation: String? = null
) {
    fun methodName() = method ?: testFactory ?: testTemplate
}

fun testId(testId: String): TestId {
    val namesByType = namesByType(testId)
    return TestId(
        className = namesByType["class"]?.firstOrNull(),
        nestedClasses = namesByType["nested-class"] ?: emptyList(),
        method = namesByType["method"]?.firstOrNull(),
        testFactory = namesByType["test-factory"]?.firstOrNull(),
        testTemplate = namesByType["test-template"]?.firstOrNull(),
        dynamicContainer = namesByType["dynamic-container"]?.firstOrNull(),
        dynamicTest = namesByType["dynamic-test"]?.firstOrNull(),
        testTemplateInvocation = namesByType["test-template-invocation"]?.firstOrNull()
    )
}

fun namesByType(testId: String): Map<String, List<String>> {
    return typeToName(testId).groupBy(
        { it.first },
        { it.second }
    ).toMap()
}

private fun typeToName(testId: String): List<Pair<String, String>> {
    return testId.split("/").mapNotNull { segment ->
        "\\[([^]]*):([^]]*)]".toRegex().find(segment)?.let {
            val type = it.groupValues[1]
            val name = it.groupValues[2]
            type to name
        }
    }
}

fun path(testId: String): List<String> {
    return with(testId(testId)) {
        ((className ?: "").split(".") + nestedClasses + listOf(
            methodName(), dynamicContainer, dynamicTest, testTemplateInvocation
        )).filterNotNull().filter { it.isNotEmpty() }
    }
}

data class TestDescriptor(
    val path: List<String>,
    val title: String,
    val source: TestSource? = null
)

/**
 * 테스트의 소스
 *
 * @property filepath    위치 (파일 경로)
 * @property line        위치 (줄)
 * @property type        타입
 * @property description 설명 ([SpecDescription] 으로 지정된)
 * @property descriptor  설명 생성기 ([SpecGeneration] 으로 지정된)
 * @property arguments   테스트 메서드의 인자
 * @property fullQualifiedName   테스트의 Full Qualified Name
 */
data class TestSource(
    val filepath: String,
    val line: Int? = null,
    val type: Type,
    val description: String? = null,
    val descriptor: SpecDescriptor? = null,
    val arguments: List<Arguments>? = null,
    val fullQualifiedName: String = ""
) : Comparable<TestSource> {
    override fun compareTo(other: TestSource) = filepath.compareTo(other.filepath).let {
        if (it == 0) {
            when {
                line == null -> -1
                other.line == null -> 1
                else -> line.compareTo(other.line)
            }
        } else {
            it
        }
    }

    enum class Type {
        PACKAGE, CLASS, METHOD
    }
}

internal fun String.parametersRemoved() = this.replace("\\(.*\\)$".toRegex(), "")

internal fun List<String>.startsWith(other: List<String>): Boolean {
    if (other.size > this.size) {
        return false
    }

    return this.subList(0, other.size) == other
}

internal fun List<String>.basePathRemoved(
    basePaths: List<String>
) = basePaths
    .asSequence()
    .map { it.split(".") }
    .filter { this.startsWith(it) }
    .map { this.subList(it.size, this.size) }
    .elementAtOrElse(0) { this }
