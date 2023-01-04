package lang.proteus

import lang.proteus.binding.Binder
import lang.proteus.evaluator.Evaluator
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

        if (tree.hasErrors()) {
            tree.printDiagnostics()
            if (filePath == null) {
                continue
            }

        }
        if (verbose)
            tree.prettyPrint()

        val binder = Binder()
        val boundExpression = binder.bindSyntaxTree(tree)
        if (binder.hasErrors()) {
            binder.printDiagnostics()
            if (filePath == null) {
                continue
            }
        }
        val evaluator = Evaluator(boundExpression)

        val result = evaluator.evaluate()
        println(result)
        if (filePath != null) {
            break
        }
    }

}
