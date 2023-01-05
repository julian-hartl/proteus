package lang.proteus.text

import lang.proteus.diagnostics.TextSpan

class TextLine(
    val text: SourceText,
    val start: Int,
    val length: Int,
    val lengthIncludingLineBreak: Int,
) {
    val span: TextSpan get() = TextSpan(start, length)
    val spanIncludingLineBreak get() = TextSpan(start, lengthIncludingLineBreak)

    override fun toString(): kotlin.String {
        return text.toString(span)
    }

}