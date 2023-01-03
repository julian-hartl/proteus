import evaluator.Evaluator
import parser.SyntaxTree
import source_file_reader.SourceFileReader

fun main(args: Array<String>) {
    val verbose = args.contains("-v")
    val filePath = args.getOrNull(0)
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

        if (tree.hasErrors()) {
            tree.outputDiagnostics()
            return
        }

        if (verbose)
            tree.prettyPrint()

        val evaluator = Evaluator(tree)

        val result = evaluator.evaluate()
        println(result)
        if (filePath != null) {
            break
        }
    }


}