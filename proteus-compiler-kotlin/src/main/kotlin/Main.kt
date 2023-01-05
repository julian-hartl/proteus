import lang.proteus.api.ProteusCompiler
import lang.proteus.printing.ConsolePrinter
import lang.proteus.printing.PrinterColor

fun main(args: Array<String>) {
    val verbose = args.contains("-v")
    val compiler = ProteusCompiler()
    val consolePrinter = ConsolePrinter()
    consolePrinter.setColor(PrinterColor.GREEN)
    while (true) {
        val line =
            run {
                consolePrinter.print("> ")
                readlnOrNull()
            } ?: continue

        if (line == "quit") {
            break
        }
        if (line.isBlank()) {
            break
        }

        try {
            compiler.compile(line, verbose = verbose)
        } catch (e: Exception) {
            e.printStackTrace()
            println()
        }
    }

}
