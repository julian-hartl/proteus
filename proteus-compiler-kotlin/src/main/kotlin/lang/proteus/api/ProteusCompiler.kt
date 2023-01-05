package lang.proteus.api

import lang.proteus.api.performance.CompilationPerformance
import lang.proteus.api.performance.ComputationTimeStopper
import lang.proteus.api.performance.PerformancePrinter
import lang.proteus.diagnostics.Diagnostics
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
            consolePrinter.println(text.toString())
        }
        val sourceText = SourceText.from(text)
        val computationTimeStopper = ComputationTimeStopper()
        computationTimeStopper.start()
        val tree = SyntaxTree.parse(text)
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
            val character = diagnostic.span.start - sourceText.lines[lineIndex].start

            printer.println()

            val prefix = text.substring(0, diagnostic.span.start)
            val error = text.substring(diagnostic.span.start, diagnostic.span.end)
            val suffix = text.substring(diagnostic.span.end)


            printer.setColor(PrinterColor.RED)
            printer.println(diagnostic.message)
            printer.reset()

            printer.print("    ")
            printer.print(prefix)
            printer.setColor(PrinterColor.RED)


            printer.print("(${lineNumber}:${character}): ")
            printer.print(error)
            printer.reset()

            printer.println(suffix)
        }
        printer.println()
    }
}