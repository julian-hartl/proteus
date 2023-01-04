package lang.proteus.diagnostics

data class Diagnostic(val message: String, val span: TextSpan) {
    override fun toString(): String {
        return message
    }
}

