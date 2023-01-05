package lang.proteus.api.performance

import lang.proteus.printing.ConsolePrinter
import lang.proteus.printing.PrinterColor

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