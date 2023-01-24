package lang.proteus.printing

import lang.proteus.diagnostics.Diagnostics
import lang.proteus.diagnostics.TextSpan

object DiagnosticsPrinter {
    fun printDiagnostics(diagnostics: Diagnostics) {
        val printer = ConsolePrinter()
        val orderedDiagnostics = diagnostics.diagnostics.sortedBy {
            it.location.sourceText.absolutePath
        }.sortedBy {
            it.span.start
        }.sortedBy {
            it.span.length
        }
        for (diagnostic in orderedDiagnostics) {

            val sourceText = diagnostic.location.sourceText
            val text = sourceText.toString()
            val fileName = diagnostic.location.fileName
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
            printer.print("$fileName(${lineNumber}:${character}) ")
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