package lang.proteus

import lang.proteus.api.Compilation
import lang.proteus.printing.ConsolePrinter
import lang.proteus.printing.PrinterColor
import lang.proteus.syntax.parser.SyntaxTree

fun main(args: Array<String>) {
    val verbose = args.contains("-v")
    while (true) {
        val line: String =
            run {
                print("> ")
                readln()
            }
        if (line == "quit") {
            break
        }
        val tree = SyntaxTree.parse(line, verbose = verbose)


        if (verbose)
            tree.prettyPrint()

        val compilation = Compilation(tree)
        val compilationResult = compilation.evaluate()
        if (compilationResult.diagnostics.hasErrors()) {
            val printer = ConsolePrinter()
            for (diagnostic in compilationResult.diagnostics.diagnostics) {
                printer.println("")

                val prefix = line.substring(0, diagnostic.span.start)
                val error = line.substring(diagnostic.span.start, diagnostic.span.end)
                val suffix = line.substring(diagnostic.span.end)

                printer.reset()

                printer.print("    ")
                printer.print(prefix)
                printer.setColor(PrinterColor.RED)
                printer.print(error)
                printer.reset()

                printer.println(suffix)
            }
            printer.println("")
        } else {

            println(compilationResult.value)
        }
    }

}
