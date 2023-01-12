package lang.proteus.diagnostics

interface Diagnostics {

    val diagnostics: List<Diagnostic>
    fun hasErrors(): Boolean

    fun concat(other: Diagnostics)


}