package lang.proteus.diagnostics

interface Diagnosable {
    fun printDiagnostics() {
        diagnostics.print()
    }

    fun hasErrors(): Boolean {
        return diagnostics.hasErrors()
    }

    val diagnostics: Diagnostics

}