package lang.proteus.diagnostics

enum class DiagnosticType {
    Error,
    Warning,
    Info
}

data class Diagnostic(val message: String, val location: TextLocation, val type: DiagnosticType) {
    override fun toString(): String {
        return message
    }

    val span get() = location.context

    val isError get() = type == DiagnosticType.Error

    val isWarning get() = type == DiagnosticType.Warning

    val isInfo get() = type == DiagnosticType.Info
}

