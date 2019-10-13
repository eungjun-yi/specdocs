package im.toss.test.specdocs

class DefaultSpecDocumentationGenerator(
    baseUri: String
) {

    private val specTableOfContentsGenerator = SpecTableOfContentsGenerator()
    private val specListGenerator = SpecListGenerator(baseUri)

    fun generate(root: Node) = """
        ${specTableOfContentsGenerator.generate(root)}
        
        ${specListGenerator.generate(root)}
    """.trimIndent()
}