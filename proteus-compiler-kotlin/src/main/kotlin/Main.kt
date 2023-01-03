import binding.Binder
import evaluator.Evaluator
import source_file_reader.SourceFileReader
import syntax.parser.SyntaxTree

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
            tree.printDiagnostics()
            continue
        }

        if (verbose)
            tree.prettyPrint()

        val binder = Binder()
        val boundExpression = binder.bindSyntaxTree(tree)
        if(binder.hasErrors()) {
            binder.printDiagnostics()
            continue
        }
        val evaluator = Evaluator(boundExpression)

        val result = evaluator.evaluate()
        println(result)
        if (filePath != null) {
            break
        }
    }


}