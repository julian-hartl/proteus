import lang.proteus.api.ProteusCompiler
import lang.proteus.external.Functions
import lang.proteus.printing.ConsolePrinter
import lang.proteus.printing.PrinterColor


fun main(args: Array<String>) {
    Functions;
    val verbose = args.contains("-v")
    val filePath = args.getOrNull(0)
    val consolePrinter = ConsolePrinter()
    if (filePath == null) {
        consolePrinter.setColor(PrinterColor.RED)
        consolePrinter.println("No file path provided. Usage: proteus <file_path> [-v]")
        return
    }
    val compiler = ProteusCompiler()
    try {
        compiler.compile(filePath)
    } catch (e: Exception) {
        e.printStackTrace()
        println()
    }

}
