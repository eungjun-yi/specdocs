package im.toss.test.specdocs

class SpecDocumentationGenerator(
    baseUri: String
) {

    private val specTableOfContentsGenerator = SpecTableOfContentsGenerator()
    private val specListGenerator = SpecListGenerator(baseUri)

    fun generate(root: Node) = specTableOfContentsGenerator.generate(root) + "\n\n" + specListGenerator.generate(root)
}
