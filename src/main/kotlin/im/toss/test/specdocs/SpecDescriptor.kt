package im.toss.test.specdocs

interface SpecDescriptor {
    fun description(precondition: Any?, table: String?): String
}
