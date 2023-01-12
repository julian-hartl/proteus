package lang.proteus.api.input

import lang.proteus.api.Compilation
import lang.proteus.diagnostics.Diagnostics
import lang.proteus.diagnostics.TextSpan
import lang.proteus.evaluator.EvaluationResult
import lang.proteus.syntax.parser.SyntaxTree
import lang.proteus.text.SourceText

internal class ProteusRepl : Repl() {

    private var previous: Compilation? = null
    private val variables = mutableMapOf<String, Any>()
    override fun evaluateSubmission(text: String) {
        val sourceText = SourceText.from(text)
        val tree = SyntaxTree.parse(sourceText)
        val compilation = Compilation(previous, tree)
        val result = compilation.evaluate(variables)
        if (result.diagnostics.hasErrors()) {
            printDiagnostics(result.diagnostics, sourceText)
        } else {
            printResult(result)
        }
        previous = compilation
    }

    override fun isCompleteSubmission(text: String): Boolean {
        val syntaxTree = SyntaxTree.parse(text)
        return !syntaxTree.hasErrors()
    }

    private fun printResult(compilationResult: EvaluationResult<*>) {
        val value = compilationResult.value
        if (value != null) {
            println()
            println(value.toString())
            println()
        }
    }

    private fun printDiagnostics(diagnostics: Diagnostics, sourceText: SourceText) {
        val text = sourceText.toString()
        for (diagnostic in diagnostics.diagnostics) {

            val lineIndex = sourceText.getLineIndex(diagnostic.span.start)
            val lineNumber = lineIndex + 1
            val line = sourceText.lines[lineIndex]
            val character = diagnostic.span.start - line.start + 1

            println()

            val prefixSpan = TextSpan.fromBounds(line.start, diagnostic.span.start)
            val prefix = sourceText.toString(prefixSpan)
            val error = text.substring(diagnostic.span.start, diagnostic.span.end)
            val suffixSpan = TextSpan.fromBounds(diagnostic.span.end, line.endIncludingLineBreak)
            val suffix = sourceText.toString(suffixSpan)


//            setColor(PrinterColor.RED)
            print("(${lineNumber}:${character}) ")
            println(diagnostic.message)

            print("    ")
            print(prefix)
//            setColor(PrinterColor.RED)


            print(error)


            println(suffix)
        }
        println()
    }

}

