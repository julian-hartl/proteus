package lang.proteus.diagnostics

import lang.proteus.text.SourceText

data class TextLocation(val sourceText: SourceText, val span: TextSpan) {
    val startLine = sourceText.getLineIndex(span.start)
    val endLine = sourceText.getLineIndex(span.end)
    val startCharacter = span.start - sourceText.lines[startLine].start
    val endCharacter = span.end - sourceText.lines[endLine].start
    val fileName = sourceText.absolutePath

    override fun toString(): String {
        return if (startLine == endLine) {
            "($startLine, $startCharacter-$endCharacter)"
        } else {
            "($startLine, $startCharacter)-($endLine, $endCharacter)"
        }
    }
}