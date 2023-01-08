import lang.proteus.api.ProteusCompiler
import lang.proteus.printing.ConsolePrinter
import lang.proteus.printing.PrinterColor

fun main(args: Array<String>) {
    val verbose = args.contains("-v")
    val compiler = ProteusCompiler()
    val consolePrinter = ConsolePrinter()
    consolePrinter.setColor(PrinterColor.GREEN)
    while (true) {
        val text =
            run {
                val textBuilder = StringBuilder()
                consolePrinter.print("> ")
                var line = readlnOrNull()
                while (line != null && line != "") {
                    textBuilder.appendLine(line)
                    consolePrinter.print("| ")
                    line = readlnOrNull()
                }
                textBuilder.toString()
            }

        if (text == "quit") {
            break
        }
        if (text.isBlank()) {
            break
        }

        try {
            compiler.compile(text, verbose = verbose)
        } catch (e: Exception) {
            e.printStackTrace()
            println()
        }
    }

}
