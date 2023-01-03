package diagnostics

data class Diagnostic(val message: String, val literal: String, val position: Int) {
    override fun toString(): String {
        return "ERROR: $message at $position: $literal"
    }
}
