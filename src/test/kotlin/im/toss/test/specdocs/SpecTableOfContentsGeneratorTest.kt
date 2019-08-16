package im.toss.test.specdocs

import im.toss.test.equalsTo
import org.junit.jupiter.api.Test

internal class SpecTableOfContentsGeneratorTest {

    @Test
    fun generate() {
        val root = Node(
            children = listOf(
                Node(
                    id = "a",
                    title = "Group A",
                    children = listOf(Node())
                ),
                Node(
                    id = "b",
                    title = "Group B",
                    children = listOf(
                        Node(
                            id = "c",
                            title = "Subgroup C",
                            children = listOf(Node())
                        ),
                        Node(
                            id = "d",
                            title = "Subgroup D",
                            source = TestSource(
                                filepath = "foo.kt",
                                line = 1,
                                type = TestSource.Type.METHOD
                            ),
                            children = listOf(Node())
                        )
                    )
                )
            )
        )

        val actual = SpecTableOfContentsGenerator().generate(root)

        val expected = """# Table Of Contents

* [Group A](#group-a)
* [Group B](#group-b)
  * [Subgroup C](#subgroup-c)
"""
        actual.equalsTo(expected)
    }
}
