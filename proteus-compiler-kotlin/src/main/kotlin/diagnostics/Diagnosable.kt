package diagnostics

interface Diagnosable {
    fun printDiagnostics()
    fun hasErrors(): Boolean

}