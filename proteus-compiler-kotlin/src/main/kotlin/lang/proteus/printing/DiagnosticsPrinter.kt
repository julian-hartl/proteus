package lang.proteus.printing

import lang.proteus.diagnostics.Diagnostics
import lang.proteus.diagnostics.TextSpan
import lang.proteus.text.SourceText

object DiagnosticsPrinter {
    fun printDiagnostics(diagnostics: Diagnostics, sourceText: SourceText) {
        val printer = ConsolePrinter()
        val text = sourceText.toString()
        for (diagnostic in diagnostics.diagnostics) {

            val lineIndex = sourceText.getLineIndex(diagnostic.span.start)
            val endLineIndex = sourceText.getLineIndex(diagnostic.span.end)
            val lineNumber = lineIndex + 1
            val line = sourceText.lines[lineIndex]
            val endLine = sourceText.lines[endLineIndex]
            val character = diagnostic.span.start - line.start + 1

            printer.println()

            val prefixSpan = TextSpan.fromBounds(line.start, diagnostic.span.start)
            val prefix = sourceText.toString(prefixSpan)
            val error = text.substring(diagnostic.span.start, diagnostic.span.end)
            val suffixSpan = TextSpan.fromBounds(diagnostic.span.end, endLine.endIncludingLineBreak)
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