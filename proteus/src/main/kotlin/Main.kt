package lang.proteus

import lang.proteus.api.Compilation
import lang.proteus.source_file_reader.SourceFileReader
import lang.proteus.syntax.parser.SyntaxTree

fun main(args: Array<String>) {
    val verbose = args.contains("-v")
    var filePath = args.getOrNull(0)
    if (filePath?.startsWith("-") == true) {
        filePath = null
    }
    while (true) {
        val input: String = if (filePath != null) {

            val fileReader = SourceFileReader()

            val sourceFile = fileReader.getSourceFile(filePath)
            println("Compiling ${sourceFile.absoluteFile}...")
            fileReader.readAndValidateSourceFile(filePath)
        } else {
            print("> ")
            readln()
        }
        if (input == "quit") {
            break
        }
        val tree = SyntaxTree.parse(input, verbose = verbose)


        if (verbose)
            tree.prettyPrint()

        val compilation = Compilation(tree)
        val compilationResult = compilation.evaluate()
        if (compilationResult.diagnostics.hasErrors()) {
            compilationResult.diagnostics.print()
            if (filePath == null) {
                continue
            }
        } else {

            println(compilationResult.value)
        }
        if (filePath != null) {
            break
        }
    }

}
