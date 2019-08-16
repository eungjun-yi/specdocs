package im.toss.test.specdocs

class SpecTableOfContentsGenerator {

    fun generate(root: Node): String {
        return "# Table Of Contents\n\n" + listDocument(root, 0)
    }

    private fun listDocument(root: Node, depth: Int): String {
        // TODO: We should make this list items of only packages and classes, not methods and dynamic-tests.
        return if (root.source?.type == TestSource.Type.METHOD) {
            ""
        } else {
            val title = root.title ?: root.id
            val (item, newDepth) = if (title != null) {
                Pair(" ".repeat(depth * 2) + "* [$title](#${link(title)})\n", depth + 1)
            } else {
                Pair("", depth)
            }
            item + root.children.joinToString("") {
                listDocument(
                    it,
                    newDepth
                )
            }
        }
    }

    private fun link(title: String) = title.toLowerCase().replace(" ", "-")
}
