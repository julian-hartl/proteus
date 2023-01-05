package lang.proteus.api

import lang.proteus.api.performance.CompilationPerformance
import lang.proteus.api.performance.ComputationTime
import lang.proteus.api.performance.ComputationTimeStopper
import lang.proteus.api.performance.PerformancePrinter
import lang.proteus.diagnostics.Diagnostics
import lang.proteus.diagnostics.TextSpan
import lang.proteus.evaluator.EvaluationResult
import lang.proteus.printing.ConsolePrinter
import lang.proteus.printing.PrinterColor
import lang.proteus.syntax.parser.SyntaxTree
import lang.proteus.text.SourceText

class ProteusCompiler(private var variables: Map<String, Any>) {
    fun compile(text: String, verbose: Boolean = false): CompilationResult {
        if (verbose) {
            val consolePrinter = ConsolePrinter()
            consolePrinter.print("Compiling line: ")
            consolePrinter.setColor(PrinterColor.BLUE)
            consolePrinter.println(text)
        }
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


        val compilation = Compilation(tree)
        val compilationResult = compilation.evaluate(variables)
        if (compilationResult.diagnostics.hasErrors()) {
            printDiagnostics(compilationResult.diagnostics, sourceText)
        } else {

            printResult(compilationResult)
            variables = compilationResult.variableContainer.untypedVariables
        }
        val performance = CompilationPerformance(
            lexerTime,
            compilationResult.parseTime,
            compilationResult.evaluationTime
        )
        if (verbose) {
            val performancePrinter = PerformancePrinter()
            performancePrinter.print(performance)
        }
        return CompilationResult(tree, compilationResult, performance)
    }

    private fun printResult(compilationResult: EvaluationResult<*>) {
        val consolePrinter = ConsolePrinter()
        consolePrinter.setColor(PrinterColor.CYAN)
        consolePrinter.println()
        consolePrinter.println(compilationResult.value?.toString() ?: "null")
        consolePrinter.println()
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