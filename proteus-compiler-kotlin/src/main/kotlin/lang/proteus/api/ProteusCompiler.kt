package lang.proteus.api

import lang.proteus.api.performance.CompilationPerformance
import lang.proteus.api.performance.ComputationTime
import lang.proteus.api.performance.ComputationTimeStopper
import lang.proteus.api.performance.PerformancePrinter
import lang.proteus.binding.ProteusType
import lang.proteus.diagnostics.Diagnostics
import lang.proteus.diagnostics.TextSpan
import lang.proteus.evaluator.EvaluationResult
import lang.proteus.printing.ConsolePrinter
import lang.proteus.printing.PrinterColor
import lang.proteus.syntax.parser.SyntaxTree
import lang.proteus.text.SourceText

internal class ProteusCompiler() {

    private var previous: Compilation? = null

    private val variables = mutableMapOf<String, Any>()

    fun compile(text: String, verbose: Boolean = false): CompilationResult {
        val sourceText = SourceText.from(text)
        val computationTimeStopper = ComputationTimeStopper()
        computationTimeStopper.start()
        val tree = SyntaxTree.parse(text)
        if (tree.hasErrors()) {
            printDiagnostics(tree.diagnostics, sourceText)
            return CompilationResult(
                tree,
                null,
                CompilationPerformance(computationTimeStopper.stop(), ComputationTime(0), ComputationTime(0))
            )
        }
        val lexerTime = computationTimeStopper.stop()


        val compilation = if (previous == null) Compilation(tree) else previous!!.continueWith(tree)
        val compilationResult = compilation.evaluate(variables)
        if (compilationResult.diagnostics.hasErrors()) {
            printDiagnostics(compilationResult.diagnostics, sourceText)
        } else {

            printResult(compilationResult)
            previous = compilation
        }
        val performance = CompilationPerformance(
            lexerTime,
            compilationResult.parseTime,
            compilationResult.evaluationTime
        )
        val performancePrinter = PerformancePrinter()
        performancePrinter.print(performance)
        return CompilationResult(tree, compilationResult, performance)
    }

    private fun printResult(compilationResult: EvaluationResult<*>) {
        val consolePrinter = ConsolePrinter()
        val value = compilationResult.value
        if (value != null) {
            val color = ProteusType.fromValueOrObject(value).getOutputColor()
            consolePrinter.setColor(color)
            consolePrinter.println()
            consolePrinter.println(value.toString())
            consolePrinter.println()
        }
    }

    private fun printDiagnostics(diagnostics: Diagnostics, sourceText: SourceText) {
        val printer = ConsolePrinter()
        val text = sourceText.toString()
        for (diagnostic in diagnostics.diagnostics) {

            val lineIndex = sourceText.getLineIndex(diagnostic.span.start)
            val lineNumber = lineIndex + 1
            val line = sourceText.lines[lineIndex]
            val character = diagnostic.span.start - line.start + 1

            printer.println()

            val prefixSpan = TextSpan.fromBounds(line.start, diagnostic.span.start)
            val prefix = sourceText.toString(prefixSpan)
            val error = text.substring(diagnostic.span.start, diagnostic.span.end)
            val suffixSpan = TextSpan.fromBounds(diagnostic.span.end, line.endIncludingLineBreak)
            val suffix = sourceText.toString(suffixSpan)



            printer.setColor(PrinterColor.RED)
            printer.print("(${lineNumber}:${character}) ")
            printer.println(diagnostic.message)
            printer.reset()

            printer.print("    ")
            printer.print(prefix)
            printer.setColor(PrinterColor.RED)


            printer.print(error)
            printer.reset()

            printer.println(suffix)
        }
        printer.println()
    }
}

private fun ProteusType.getOutputColor(): PrinterColor {
    return when (this) {
        ProteusType.Int -> PrinterColor.BLUE
        ProteusType.Boolean -> PrinterColor.MAGENTA
        ProteusType.Object -> PrinterColor.CYAN
        ProteusType.Type -> PrinterColor.RED
        ProteusType.String -> PrinterColor.GREEN
        ProteusType.Char -> PrinterColor.BLACK
        ProteusType.BinaryString -> PrinterColor.BLUE
        ProteusType.Range -> PrinterColor.MAGENTA
    }
}