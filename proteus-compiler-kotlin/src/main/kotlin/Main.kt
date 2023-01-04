import lang.proteus.api.Compilation
import lang.proteus.printing.ConsolePrinter
import lang.proteus.printing.PrinterColor
import lang.proteus.syntax.parser.SyntaxTree

fun main(args: Array<String>) {
    val verbose = args.contains("-v")
    var variables: Map<String, Any> = mapOf(
        "x" to 1
    )
    while (true) {
        val line =
            run {
                print("> ")
                readlnOrNull()
            } ?: continue
        if (line == "quit") {
            break
        }
        val tree = SyntaxTree.parse(line, verbose = verbose)


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
    }

}
