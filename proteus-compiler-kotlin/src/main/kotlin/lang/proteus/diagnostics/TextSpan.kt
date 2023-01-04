package lang.proteus.diagnostics

data class TextSpan(val start: Int, val literal: String) {
    val end: Int
        get() = start + literal.length
}