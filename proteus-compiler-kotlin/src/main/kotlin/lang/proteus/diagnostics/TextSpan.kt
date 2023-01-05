package lang.proteus.diagnostics

data class TextSpan(val start: Int, val length: Int) {

    companion object {
        fun fromLiteral(start: Int, literal: String): TextSpan {
            return TextSpan(start, literal.length)
        }

        fun fromBounds(start: Int, end: Int): TextSpan {
            if(end < start) {
                throw IllegalArgumentException("end must be greater than start")
            }
            return TextSpan(start, end - start)
        }
    }

    val end: Int
        get() = start + length
}