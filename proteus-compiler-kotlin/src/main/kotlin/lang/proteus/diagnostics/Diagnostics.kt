package lang.proteus.diagnostics

interface Diagnostics {
    fun print()

    val diagnostics: List<Diagnostic>
    fun hasErrors(): Boolean

    fun concat(other: Diagnostics)
}