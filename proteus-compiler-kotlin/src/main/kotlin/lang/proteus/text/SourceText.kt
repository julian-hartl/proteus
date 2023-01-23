package lang.proteus.text

import lang.proteus.diagnostics.TextSpan

class SourceText(private val text: String, val fileName: String) {

    val lines: List<TextLine> = parseLines(this, text)

    val length get() = text.length

    operator fun get(index: Int): Char = text[index]

    fun getLineIndex(position: Int): Int {
        var lower = 0
        var upper = lines.size - 1
        while (lower <= upper) {
            val index = lower + (upper - lower) / 2
            val start = lines[index].start

            if (position == start) {
                return index
            }
            if (start > position) {
                upper = index - 1
            } else {
                lower = index + 1
            }
        }
        return lower - 1
    }

    override fun toString(): String {
        return text
    }

    fun toString(start: Int, length: Int): String {
        return text.substring(start, start + length)
    }

    fun toString(span: TextSpan): String {
        return toString(span.start, span.length)
    }

    companion object {
        fun from(text: String): SourceText {
            return SourceText(text, "<none>")
        }

        fun parseLines(sourceText: SourceText, text: String): List<TextLine> {
            val lines = mutableListOf<TextLine>()
            var position = 0
            var lineStart = 0
            while (position < text.length) {
                val lineBreakWidth = getLineBreakWidth(text, position)

                if (lineBreakWidth == 0) {
                    position++
                } else {
                    val lineLength = position - lineStart
                    val lengthIncludingLineBreak = lineLength + lineBreakWidth
                    val line = TextLine(sourceText, lineStart, lineLength, lengthIncludingLineBreak)
                    lines.add(line)
                    position += lineBreakWidth
                    lineStart = position
                }
            }

            if (position >= lineStart) {
                val lineLength = position - lineStart
                val line = TextLine(sourceText, lineStart, position - lineStart, lineLength)
                lines.add(line)
            }

            return lines.toList()
        }

        private fun getLineBreakWidth(text: String, index: Int): Int {
            val c = text[index]
            val l = if (index + 1 >= text.length) '\u0000' else text[index + 1]

            return when (c) {
                '\r' -> if (l == '\n') 2 else 1
                '\n' -> 1
                else -> 0
            }
        }

    }
}