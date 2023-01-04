package lang.proteus.diagnostics

interface Diagnostics {
    fun size(): Int
    fun print()

    val diagnostics: List<Diagnostic>
}