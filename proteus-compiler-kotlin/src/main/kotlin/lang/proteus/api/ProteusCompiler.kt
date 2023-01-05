package lang.proteus.api

import lang.proteus.api.performance.CompilationPerformance
import lang.proteus.api.performance.ComputationTime
import lang.proteus.api.performance.ComputationTimeStopper
import lang.proteus.printing.ConsolePrinter
import lang.proteus.printing.PrinterColor
import lang.proteus.syntax.parser.SyntaxTree

class PerformancePrinter {

    private val consolePrinter = ConsolePrinter()
    fun print(performance: CompilationPerformance) {
        printComputationTime("Lexing", performance.lexingTime)
        printComputationTime("Parsing", performance.parsingTime)
        printComputationTime("Evaluation", performance.evaluationTime)
    }

    private fun printComputationTime(prefix: String, time: ComputationTime) {
        consolePrinter.setColor(PrinterColor.WHITE)
        consolePrinter.print("$prefix: ")
        consolePrinter.setColor(getColorFromTime(time.time))
        consolePrinter.print(formatTime(time.time))
        consolePrinter.println()
    }

    private fun getColorFromTime(time: Long): PrinterColor {
        return when {
            time < 10 -> PrinterColor.GREEN
            time < 100 -> PrinterColor.YELLOW
            else -> PrinterColor.RED
        }
    }

    private fun formatTime(time: Long): String {
        return if (time < 1000) {
            "${time}ms"
        } else {
            "${time / 1000}s"
        }
    }
}

class ProteusCompiler(private var variables: Map<String, Any>) {
    fun compile(line: String, verbose: Boolean = false): CompilationResult {
        val computationTimeStopper = ComputationTimeStopper()
        computationTimeStopper.start()
        val tree = SyntaxTree.parse(line, verbose = verbose)
        val lexerTime = computationTimeStopper.stop()

        if (verbose)
            tree.prettyPrint()

        val compilation = Compilation(tree)
        val compilationResult = compilation.evaluate(variables)
        if (compilationResult.diagnostics.hasErrors()) {
            val printer = ConsolePrinter()
            for (diagnostic in compilationResult.diagnostics.diagnostics) {
                printer.println()

                val prefix = line.substring(0, diagnostic.span.start)
                val error = line.substring(diagnostic.span.start, diagnostic.span.end)
                val suffix = line.substring(diagnostic.span.end)


                printer.setColor(PrinterColor.RED)
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
        } else {

            println(compilationResult.value)
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
}