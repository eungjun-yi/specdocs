package im.toss.test.specdocs

import im.toss.test.equalsTo
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

internal class SpecDocumentationTreeBuilderTest {
    @TestFactory
    fun generateTree(): List<DynamicTest> {
        data class Params(
            val tests: List<TestItem>,
            val expectedTree: Node
        )
        return listOf(
            Params(
                listOf(
                    TestItem(
                        "[engine:junit-jupiter]/[class:a.b.c.Foo]/[method:foo]",
                        "test 1"
                    )
                ),
                Node(
                    children = listOf(
                        Node(
                            "a",
                            children = listOf(
                                Node(
                                    "b",
                                    children = listOf(
                                        Node(
                                            "c",
                                            children = listOf(
                                                Node(
                                                    "Foo",
                                                    children = listOf(
                                                        Node("foo", "test 1")
                                                    )
                                                )
                                            )
                                        )
                                    )
                                )

                            )
                        )

                    )
                )
            ),
            Params(
                listOf(
                    TestItem(
                        "[engine:junit-jupiter]/[class:a.b.c.Foo]/[method:foo1]",
                        "test 1"
                    ),
                    TestItem(
                        "[engine:junit-jupiter]/[class:a.b.c.d.Bar]/[method:foo2]",
                        "test 2"
                    )
                ),
                Node(
                    children = listOf(
                        Node(
                            "a",
                            children = listOf(
                                Node(
                                    "b",
                                    children = listOf(
                                        Node(
                                            "c",
                                            children = listOf(
                                                Node(
                                                    "Foo",
                                                    children = listOf(
                                                        Node("foo1", "test 1")
                                                    )
                                                ),
                                                Node(
                                                    "d",
                                                    children = listOf(
                                                        Node(
                                                            "Bar",
                                                            children = listOf(
                                                                Node("foo2", "test 2")
                                                            )
                                                        )
                                                    )
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        ).map {
            dynamicTest("Tree Generation Test") {
                val treeBuilder = SpecDocumentationTreeBuilder()
                treeBuilder.addNodes(it.tests)
                treeBuilder.build().equalsTo(it.expectedTree)
            }
        }
    }

    @TestFactory
    fun testParsingIdToPath(): List<DynamicTest> = listOf(
        dynamicTest("a.b.c.Foo.foo1") {
            val parsed =
                path("[engine:junit-jupiter]/[class:a.b.c.Foo]/[method:foo1]")
            parsed.equalsTo(listOf("a", "b", "c", "Foo", "foo1"))
        },
        dynamicTest("a.b.c.d") {
            val parsed = path("[engine:junit-jupiter]/[class:a.b.c.d]")
            parsed.equalsTo(listOf("a", "b", "c", "d"))
        },
        dynamicTest("a.b.c.d.Foo (nested class)") {
            val parsed = path("[engine:junit-jupiter]/[class:a.b.c.d]/[nested-class:Foo]")
            parsed.equalsTo(listOf("a", "b", "c", "d", "Foo"))
        },
        dynamicTest("a.b.c.d.Foo.Bar (nested classes)") {
            val parsed = path("[engine:junit-jupiter]/[class:a.b.c.d]/[nested-class:Foo]/[nested-class:Bar]")
            parsed.equalsTo(listOf("a", "b", "c", "d", "Foo", "Bar"))
        },
        dynamicTest("a.b.c.d.Foo.bar() (nested classes)") {
            val parsed = path("[engine:junit-jupiter]/[class:a.b.c.d]/[nested-class:Foo]/[method:bar()]")
            parsed.equalsTo(listOf("a", "b", "c", "d", "Foo", "bar()"))
        },
        dynamicTest("bad path should be ignored") {
            val parsed = path("///")
            parsed.equalsTo(emptyList<String>())
        }
    )

    @TestFactory
    fun testTestItemToPathAndContents(): List<DynamicTest> =
        listOf(
            TestItem(
                uniqueId = "[engine:junit-jupiter]/[class:a.b.c.Foo]/[method:foo1]",
                displayName = "Test 1"
            ) to TestDescriptor(
                listOf("a", "b", "c", "Foo", "foo1"),
                title = "Test 1"
            ),
            TestItem(
                uniqueId = "[engine:junit-jupiter]/[class:a.b.c.Foo]/[method:foo2]",
                displayName = "Test 2"
            ) to TestDescriptor(
                listOf("a", "b", "c", "Foo", "foo2"),
                title = "Test 2"
            )
        ).map {
            dynamicTest(it.first.uniqueId) {
                it.first.toTestDescriptor().equalsTo(it.second)
            }
        }

    @TestFactory
    fun testMergingPathAndTitle(): List<DynamicTest> =
        listOf(
            listOf(
                TestDescriptor(
                    listOf("a", "Foo", "foo1"),
                    title = "Test 1"
                )
            ) to Node(
                children = listOf(
                    Node(
                        "a",
                        children = listOf(
                            Node(
                                "Foo",
                                children = listOf(
                                    Node("foo1", "Test 1")
                                )
                            )
                        )
                    )
                )
            ),
            listOf(
                TestDescriptor(
                    listOf("a", "Foo", "foo1"),
                    title = "Test 1"
                ),
                TestDescriptor(
                    listOf("a", "b", "Bar", "bar1"),
                    title = "Test 2"
                )
            ) to Node(
                children = listOf(
                    Node(
                        "a",
                        children = listOf(
                            Node(
                                "Foo",
                                children = listOf(Node("foo1", "Test 1"))
                            ),
                            Node(
                                "b",
                                children = listOf(
                                    Node(
                                        "Bar",
                                        children = listOf(Node("bar1", "Test 2"))
                                    )
                                )
                            )
                        )
                    )
                )
            )
        ).map {
            dynamicTest("${it.first}") {
                SpecDocumentationTreeBuilder().merge(it.first).equalsTo(it.second)
            }
        }

    @TestFactory
    fun testMerging(): List<DynamicTest> = listOf(
        dynamicTest("test 0") {
            SpecDocumentationTreeBuilder.merge(
                Node(),
                TestDescriptor(
                    path = listOf("a"),
                    title = "t0"
                )
            ).equalsTo(
                Node(
                    null,
                    children = listOf(
                        Node("a", "t0")
                    )
                )
            )
        },
        dynamicTest("test 1") {
            SpecDocumentationTreeBuilder.merge(
                Node(),
                TestDescriptor(
                    path = listOf("a", "b"),
                    title = "t1"
                )
            ).equalsTo(
                Node(
                    null,
                    children = listOf(
                        Node(
                            "a",
                            children = listOf(
                                Node("b", "t1")
                            )
                        )
                    )
                )
            )
        },
        dynamicTest("test 2") {
            SpecDocumentationTreeBuilder.merge(
                Node(),
                TestDescriptor(
                    path = listOf("a", "b", "c"),
                    title = "t2"
                )
            ).equalsTo(
                Node(
                    null,
                    children = listOf(
                        Node(
                            "a",
                            children = listOf(
                                Node(
                                    "b",
                                    children = listOf(
                                        Node(id = "c", title = "t2")
                                    )
                                )
                            )
                        )
                    )
                )
            )
        },
        dynamicTest("test 3") {
            SpecDocumentationTreeBuilder.merge(
                Node(
                    children = listOf(
                        Node(
                            "a",
                            children = listOf(
                                Node("t1")
                            )
                        )
                    )
                ),
                TestDescriptor(
                    path = listOf("a", "b", "c"),
                    title = "t3"
                )
            ).equalsTo(
                Node(
                    null,
                    children = listOf(
                        Node(
                            "a",
                            children = listOf(
                                Node("t1"),
                                Node(
                                    "b",
                                    children = listOf(
                                        Node("c", "t3")
                                    )
                                )
                            )
                        )
                    )
                )
            )
        }
    )

    @Test
    fun mergeWithItemWhosePathIsEmpty() {
        SpecDocumentationTreeBuilder.merge(
            Node(),
            TestDescriptor(path = listOf(), title = "t0")
        ).equalsTo(
            Node(null, children = emptyList())
        )
    }
}
