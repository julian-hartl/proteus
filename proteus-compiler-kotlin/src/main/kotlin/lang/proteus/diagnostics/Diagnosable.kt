package lang.proteus.diagnostics

interface Diagnosable {
    fun hasErrors(): Boolean {
        return diagnostics.hasErrors()
    }

    val diagnostics: Diagnostics

}