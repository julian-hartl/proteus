import lang.proteus.api.ProteusCompiler
import lang.proteus.api.input.ConsoleInputReader
import lang.proteus.api.input.SourceFileReader
import lang.proteus.printing.ConsolePrinter
import lang.proteus.printing.PrinterColor


private val defaultInputReader = ConsoleInputReader()

private const val exampleSourcePath = "../examples/hello-world.psl"

fun main(args: Array<String>) {
    val verbose = args.contains("-v")
    val useConsoleInput = args.contains("-c")
    val compiler = ProteusCompiler()
    val consolePrinter = ConsolePrinter()
    consolePrinter.setColor(PrinterColor.GREEN)
    val textInputReader = if (useConsoleInput) ConsoleInputReader() else SourceFileReader(exampleSourcePath)
    var text = textInputReader.read()
    while (text != null) {
        try {
            compiler.compile(text, verbose = verbose)
            text = textInputReader.read()
        } catch (e: Exception) {
            e.printStackTrace()
            println()
        }
    }

}
