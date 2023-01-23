import lang.proteus.api.ProteusCompiler
import lang.proteus.api.input.SourceFileReader
import lang.proteus.external.Functions
import lang.proteus.printing.ConsolePrinter
import lang.proteus.printing.PrinterColor


fun main(args: Array<String>) {
    Functions;
    val verbose = args.contains("-v")
    val filePath = args.getOrNull(0)
    if (filePath == null) {
        println("No file path provided. Usage: proteus <file_path> [-v]")
        return
    }
    val compiler = ProteusCompiler()
    val consolePrinter = ConsolePrinter()
    consolePrinter.setColor(PrinterColor.GREEN)
    val textInputReader = SourceFileReader(filePath)
    var text = textInputReader.read()
    while (text != null) {
        try {
            compiler.compile(text, verbose = verbose, generateCode = true)
            text = textInputReader.read()
        } catch (e: Exception) {
            e.printStackTrace()
            println()
        }
    }

}
