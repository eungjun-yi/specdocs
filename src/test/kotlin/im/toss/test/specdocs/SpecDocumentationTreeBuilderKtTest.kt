package im.toss.test.specdocs

import im.toss.test.equalsTo
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class SpecDocumentationTreeBuilderKtTest {

    @Test
    fun startsWith() {
        listOf("a", "b").startsWith(listOf("a")).equalsTo(true)
        listOf("a", "b").startsWith(listOf("b")).equalsTo(false)
        listOf("a", "b").startsWith(listOf("a", "b", "c")).equalsTo(false)
    }

    @Test
    fun `source of dynamic test or dynamic container is null`() {
        source("[engine:junit-jupiter]/[class:a.b.c.Foo]/[test-factory:foo1]/[dynamic-test:bar1]").equalsTo(null)
        source("[engine:junit-jupiter]/[class:a.b.c.Foo]/[test-factory:foo1]/[dynamic-container:bar1]").equalsTo(null)
    }

    @Test
    fun `source of method of nested class`() {
        source("[engine:junit-jupiter]/[class:im.toss.test.specdocs.Foo]/[nested-class:Bar]/[method:bar()]")!!.fullQualifiedName.equalsTo("im.toss.test.specdocs.Foo\$Bar")
    }
}

class Foo {

    @Nested
    inner class Bar {

        @Test
        fun bar() {

        }
    }
}
