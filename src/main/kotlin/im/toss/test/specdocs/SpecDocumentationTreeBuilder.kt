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
    path = parseId(this.uniqueId),
    title = this.displayName.parametersRemoved(),
    source = source(this.uniqueId)
)

fun source(testId: String): TestSource? {
    val matched = uniqueIdPattern.find(testId)!!

    val classPath = java.net.URLDecoder.decode(matched.groupValues[2], "UTF-8")
    val methodName = java.net.URLDecoder.decode(matched.groupValues[5], "UTF-8")
    val dynamicContainer = java.net.URLDecoder.decode(matched.groupValues[7], "UTF-8")
    val dynamicTest = java.net.URLDecoder.decode(matched.groupValues[9], "UTF-8")

    if (classPath.isBlank()) {
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
        val ctClass = classPool.get(classPath)
        if (methodName.isNotEmpty()) {
            methodSource(methodName, classPath, ctClass)
        } else {
            classSource(classPath, ctClass)
        }
    } catch (e: NotFoundException) {
        null
    }
}

private fun classSource(
    classPath: String,
    ctClass: CtClass
): TestSource {

    val description = ctClass.getAnnotation(SpecDescription::class.java) as SpecDescription?

    return TestSource(
        filepath = filepath(classPath, ctClass),
        type = TestSource.Type.CLASS,
        description = description?.value
    )
}

private fun filepath(classPath: String, ctClass: CtClass) =
    filepath(classPath, ctClass.classFile.sourceFile)

private fun filepath(classPath: String, file: String?) =
    (classPath.split(".").let { it.subList(0, it.size - 1) } + file).joinToString("/")

private fun methodSource(
    methodName: String,
    classPath: String,
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
            Class.forName(classPath).getMethod(sourceMethodName).invoke(null) as List<Arguments>
        }
    }

    val correctionOffset = -1 // To make this pos the method header not the body
    val line = method.methodInfo.getLineNumber(0) + correctionOffset

    val descriptor = specGeneration?.value?.createInstance()

    return TestSource(
        filepath = filepath(classPath, ctClass),
        type = TestSource.Type.METHOD,
        line = line,
        description = description?.value,
        descriptor = descriptor,
        arguments = arguments
    )
}

fun parseId(testId: String): List<String> {
    val matched = uniqueIdPattern.find(testId)!!

    val klass = matched.groupValues[2]
    val method = matched.groupValues[5]
    val dynamicContainer = matched.groupValues[7]
    val dynamicTest = matched.groupValues[9]
    val testTemplateInvocation = matched.groupValues[11]

    return klass.split(".") + listOf(
        method, dynamicContainer, dynamicTest, testTemplateInvocation
    ).filter { it.isNotEmpty() }
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
 */
data class TestSource(
    val filepath: String,
    val line: Int? = null,
    val type: Type,
    val description: String? = null,
    val descriptor: SpecDescriptor? = null,
    val arguments: List<Arguments>? = null
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

private val uniqueIdPattern =
    "\\[engine:[^]]*](/\\[class:([^]]*)])?(/\\[(method|test-factory|test-template):([^]]*)])?(/\\[dynamic-container:([^]]*)])?(/\\[dynamic-test:([^]]*)])?(/\\[test-template-invocation:([^]]*)])?".toRegex()

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