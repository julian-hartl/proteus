package syntax.parser

import diagnostics.Diagnosable
import diagnostics.Diagnostics
import syntax.lexer.SyntaxToken

class SyntaxTree(
    val root: ExpressionSyntax,
    private val endOfFileToken: SyntaxToken<*>,
    private val diagnostics: Diagnostics
) : Diagnosable {

    companion object {
        fun parse(text: String, verbose: Boolean): SyntaxTree {
            val parser = Parser(text, verbose = verbose)
            return parser.parse()
        }
    }

    fun prettyPrint() {
        h_prettyPrint(root, "")
    }

    private fun h_prettyPrint(node: SyntaxNode, indent: String) {
        var currentIndent = indent
        print(indent)
        print(node.kind)

        if (node is SyntaxToken<*>) {
            print(" " + node.value)
        }

        println()

        currentIndent += "    ";
        for (child in node.getChildren()) {
            h_prettyPrint(child, currentIndent)
        }
    }


    override fun hasErrors(): Boolean {
        return diagnostics.size() > 0
    }

    override fun printDiagnostics() {
        diagnostics.print()
    }
}