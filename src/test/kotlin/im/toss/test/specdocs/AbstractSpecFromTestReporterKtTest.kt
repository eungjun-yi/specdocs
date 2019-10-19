package im.toss.test.specdocs

import im.toss.test.equalsTo
import org.junit.jupiter.api.Test

internal class AbstractSpecFromTestReporterKtTest {

    @Test
    fun findByName() {
        val c = Node(
            source = TestSource(filepath = "", type = TestSource.Type.CLASS, fullQualifiedName = "a.b.c")
        )

        val b = Node(
            source = TestSource(filepath = "", type = TestSource.Type.CLASS, fullQualifiedName = "a.b"),
            children = listOf(c)
        )

        val a = Node(
            source = TestSource(filepath = "", type = TestSource.Type.CLASS, fullQualifiedName = "a"),
            children = listOf(b)
        )

        a.findByName("a").equalsTo(a)
        a.findByName("a.b").equalsTo(b)
        a.findByName("a.b.c").equalsTo(c)
    }

    @Test
    fun `FindByName returns null if the node has no source`() {
        Node(source = null).findByName("a").equalsTo(null)
    }

    @Test
    fun `FindByName returns null if nothing is matched`() {
        val node = Node(
            source = TestSource(filepath = "", type = TestSource.Type.CLASS, fullQualifiedName = "a"),
            children = listOf(Node())
        )
        node.findByName("b").equalsTo(null)
    }
}