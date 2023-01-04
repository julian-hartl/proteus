package lang.proteus.diagnostics

interface Diagnosable {
    fun printDiagnostics()
    fun hasErrors(): Boolean

}