package lang.proteus.evaluation

import lang.proteus.diagnostics.TextSpan
import java.io.StringReader
import java.util.*
import kotlin.math.min

internal data class AnnotatedText(val text: String, val spans: List<TextSpan>) {

    companion object {
        fun parse(text: String): AnnotatedText {
            val text = unindent(text)

            val textBuilder = StringBuilder()
            val spans = mutableListOf<TextSpan>()
            val startStack = Stack<Int>()

            var position = 0;

            for (c in text) {
                when (c) {
                    '[' -> {
                        startStack.push(position)
                    }

                    ']' -> {
                        if (startStack.isEmpty()) {
                            throw IllegalArgumentException("Missing '['.")
                        }
                        val start = startStack.pop()
                        val end = position
                        val span = TextSpan.fromBounds(start, end)
                        spans.add(span)
                    }

                    else -> {
                        position++;
                        textBuilder.append(c)
                    }
                }
            }

            if (!startStack.isEmpty()) {
                throw IllegalArgumentException("Missing ']'.")
            }
            return AnnotatedText(textBuilder.toString(), spans)
        }

        fun unindentLines(text: String): List<String> {
            val stringReader = StringReader(text)
            val lines = stringReader.readLines().toMutableList()
            var minIndentation = Int.MAX_VALUE
            for ((index, line) in lines.withIndex()) {
                if (line.trim().isEmpty()) {
                    lines[index] = ""
                    continue
                }
                val indentation = line.length - line.trimStart().length;
                minIndentation = min(minIndentation, indentation)
            }

            for ((index, line) in lines.withIndex()) {
                if (line.isEmpty()) {
                    continue
                }
                lines[index] = line.substring(minIndentation)
            }


            while (lines.size > 0 && lines.first().isEmpty()) {
                lines.removeFirst()
            }

            while (lines.size > 0 && lines.last().isEmpty()) {
                lines.removeLast()
            }
            return lines
        }

        private fun unindent(text: String): String {

            val lines = unindentLines(text)

            return lines.joinToString(System.lineSeparator())
        }
    }

}