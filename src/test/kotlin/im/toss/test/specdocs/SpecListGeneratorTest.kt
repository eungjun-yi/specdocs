package im.toss.test.specdocs

import im.toss.test.equalsTo
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory

internal class SpecListGeneratorTest {
    @TestFactory
    fun generateDocument(): List<DynamicTest> = listOf(
        listOf(
            TestItem(
                "[engine:junit-jupiter]/[class:a.b.c.Foo]/[method:foo]",
                "test 1"
            )
        ) to "# a\n\n## b\n\n### c\n\n#### Foo\n\n* test 1" ,
        listOf(
            TestItem(
                "[engine:junit-jupiter]/[class:a.b.c.Foo]/[method:foo1]",
                "test 1"
            ),
            TestItem(
                "[engine:junit-jupiter]/[class:a.b.c.d.Bar]/[method:foo2]",
                "test 2"
            )
        ) to "# a\n\n## b\n\n### c\n\n#### Foo\n\n* test 1\n\n#### d\n\n##### Bar\n\n* test 2" ,
        listOf(
            TestItem(
                "[engine:junit-jupiter]/[class:a.b.c.Foo]/[method:foo1]",
                "testgroup 1"
            ),
            TestItem(
                "[engine:junit-jupiter]/[class:a.b.c.Foo]/[test-factory:foo1]/[dynamic-test:bar1]",
                "subtest 1"
            ),
            TestItem(
                "[engine:junit-jupiter]/[class:a.b.c.Foo]/[method:foo2]",
                "test 2"
            ),
            TestItem(
                "[engine:junit-jupiter]/[class:a.b.c.Foo]/[method:foo3]",
                "test 3"
            )
        ) to "# a\n\n## b\n\n### c\n\n#### Foo\n\n* test 2\n* test 3\n\n##### testgroup 1\n\n* subtest 1",
        listOf(
            TestItem(
                uniqueId = "[engine:junit-jupiter]/[class:im.toss.test.specter.examples.ArgumentTest]/[test-template:test(java.lang.String, java.lang.String)]/[test-template-invocation:#1]",
                displayName = "[1] a, A"
            ),
            TestItem(
                uniqueId = "[engine:junit-jupiter]/[class:im.toss.test.specter.examples.ArgumentTest]/[test-template:test(java.lang.String, java.lang.String)]/[test-template-invocation:#2]",
                displayName = "[2] b, B"
            ),
            TestItem(
                uniqueId = "[engine:junit-jupiter]/[class:im.toss.test.specter.examples.ArgumentTest]/[test-template:test(java.lang.String, java.lang.String)]",
                displayName = "test(String, String)"
            ),
            TestItem(
                uniqueId = "[engine:junit-jupiter]/[class:im.toss.test.specter.examples.ArgumentTest]",
                displayName = "ArgumentTest"
            )
        ) to """
            # im

            ## toss

            ### test

            #### specter

            ##### examples

            ###### [ArgumentTest](/im/toss/test/specter/examples/ArgumentTest.kt)

            ####### [test](/im/toss/test/specter/examples/ArgumentTest.kt#L24)

            | String | String |
            | ------ | ------ |
            | a | A |
            | b | B |
            """.trimIndent(),
        listOf(
            TestItem(
                "[engine:junit-jupiter]/[class:im.toss.test.specter.examples.SpecDescriptionTest]/[method:test]",
                "test"
            ),
            TestItem(
                uniqueId = "[engine:junit-jupiter]/[class:im.toss.test.specter.examples.SpecDescriptionTest]",
                displayName = "SpecDescriptionTest"
            )
        ) to """
            # im

            ## toss

            ### test

            #### specter

            ##### examples

            ###### [SpecDescriptionTest](/im/toss/test/specter/examples/SpecDescriptionTest.kt)

            hello, test

            * [test](/im/toss/test/specter/examples/SpecDescriptionTest.kt#L11)
            """.trimIndent(),
        listOf(
            TestItem(
                uniqueId = "[engine:junit-jupiter]/[class:im.toss.test.specter.examples.SpecGenerationTest]/[test-template:test(java.lang.String, java.lang.String)]/[test-template-invocation:#1]",
                displayName = "[1] a, A"
            ),
            TestItem(
                uniqueId = "[engine:junit-jupiter]/[class:im.toss.test.specter.examples.SpecGenerationTest]/[test-template:test(java.lang.String, java.lang.String)]/[test-template-invocation:#2]",
                displayName = "[2] b, B"
            ),
            TestItem(
                uniqueId = "[engine:junit-jupiter]/[class:im.toss.test.specter.examples.SpecGenerationTest]/[test-template:test(java.lang.String, java.lang.String)]",
                displayName = "test(String, String)"
            ),
            TestItem(
                uniqueId = "[engine:junit-jupiter]/[class:im.toss.test.specter.examples.SpecGenerationTest]",
                displayName = "SpecGenerationTest"
            )
        ) to """
            # im

            ## toss

            ### test

            #### specter

            ##### examples

            ###### [SpecGenerationTest](/im/toss/test/specter/examples/SpecGenerationTest.kt)

            ####### [test](/im/toss/test/specter/examples/SpecGenerationTest.kt#L17)

            Capitalize the given string.

            | If | Then |
            | ---- | ---- |
            | a | A |
            | b | B |
            """.trimIndent(),
        listOf(
            TestItem(
                uniqueId = "[engine:junit-jupiter]/[class:im.toss.test.specter.examples.SpecGenerationTest]/[test-template:test2(java.lang.String, java.lang.String)]/[test-template-invocation:#1]",
                displayName = "[1] a, A"
            ),
            TestItem(
                uniqueId = "[engine:junit-jupiter]/[class:im.toss.test.specter.examples.SpecGenerationTest]/[test-template:test2(java.lang.String, java.lang.String)]/[test-template-invocation:#2]",
                displayName = "[2] b, B"
            ),
            TestItem(
                uniqueId = "[engine:junit-jupiter]/[class:im.toss.test.specter.examples.SpecGenerationTest]/[test-template:test2(java.lang.String, java.lang.String)]",
                displayName = "test2(String, String)"
            ),
            TestItem(
                uniqueId = "[engine:junit-jupiter]/[class:im.toss.test.specter.examples.SpecGenerationTest]",
                displayName = "SpecGenerationTest"
            )
        ) to """
            # im

            ## toss

            ### test

            #### specter

            ##### examples

            ###### [SpecGenerationTest](/im/toss/test/specter/examples/SpecGenerationTest.kt)

            ####### [test2](/im/toss/test/specter/examples/SpecGenerationTest.kt#L25)

            Capitalize the given string.

            | If | Then |
            | ---- | ---- |
            | a | A |
            | b | B |
            """.trimIndent()
    ).map { (given, expected) ->
        dynamicTest("Document test") {
            val treeBuilder = SpecDocumentationTreeBuilder()
            treeBuilder.addNodes(given)
            val root = treeBuilder.build()
            SpecListGenerator("").generate(root).equalsTo(expected)
        }
    }
}
