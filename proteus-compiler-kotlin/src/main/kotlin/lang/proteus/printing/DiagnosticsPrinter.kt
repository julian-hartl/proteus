package lang.proteus.printing

import lang.proteus.diagnostics.Diagnostics
import lang.proteus.diagnostics.TextSpan
import lang.proteus.text.SourceText

object DiagnosticsPrinter {
    fun printDiagnostics(diagnostics: Diagnostics, sourceText: SourceText) {
        val printer = ConsolePrinter()
        val text = sourceText.toString()
        val orderedDiagnostics = diagnostics.diagnostics.sortedBy {
            it.location.sourceText.fileName
        }.sortedBy {
            it.span.start
        }.sortedBy {
            it.span.length
        }
        for (diagnostic in orderedDiagnostics) {

            val span = diagnostic.span
            val lineIndex = sourceText.getLineIndex(span.start)
            val endLineIndex = sourceText.getLineIndex(span.end)
            val lineNumber = lineIndex + 1
            val line = sourceText.lines[lineIndex]
            val endLine = sourceText.lines[endLineIndex]
            val character = span.start - line.start + 1

            printer.println()

            val prefixSpan = TextSpan.fromBounds(line.start, span.start)
            val prefix = sourceText.toString(prefixSpan)
            val error = text.substring(span.start, span.end)
            val suffixSpan = TextSpan.fromBounds(span.end, endLine.endIncludingLineBreak)
            val suffix = sourceText.toString(suffixSpan)


            val color = if (diagnostic.isError) PrinterColor.RED else PrinterColor.YELLOW
            printer.setColor(color)
            printer.print("(${lineNumber}:${character}) ")
            printer.println(diagnostic.message)
            printer.reset()

            printer.print("    ")
            printer.print(prefix)
            printer.setColor(color)


            printer.print(error)
            printer.reset()

            printer.println(suffix)
        }
        printer.println()
    }

}