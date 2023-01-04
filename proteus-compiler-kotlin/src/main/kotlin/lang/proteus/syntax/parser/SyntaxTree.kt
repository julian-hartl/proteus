package lang.proteus.syntax.parser

import lang.proteus.diagnostics.Diagnosable
import lang.proteus.diagnostics.Diagnostics
import lang.proteus.syntax.lexer.SyntaxToken

class SyntaxTree(
    val root: ExpressionSyntax,
    private val endOfFileToken: SyntaxToken<*>,
    override val diagnostics: Diagnostics
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
        if (node is ExpressionSyntax) {
            print(node::class.simpleName)
            currentIndent += "  "
        } else {
            print(node.token)
        }
        if (node is SyntaxToken<*>) {
            if (node.value != null) {
                print(" = ")
                print(node.value)
            }
        }

        println()

        currentIndent += "    ";
        for (child in node.getChildren()) {
            h_prettyPrint(child, currentIndent)
        }
    }


}