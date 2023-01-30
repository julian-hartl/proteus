package lang.proteus.diagnostics

interface Diagnostics {
    fun print()

    val errors: List<Diagnostic>
    val warnings: List<Diagnostic>

    fun hasErrors(): Boolean {
        return this.errors.isNotEmpty()
    }

    fun hasWarnings(): Boolean {
        return this.warnings.isNotEmpty()
    }

    fun concat(other: Diagnostics)


}