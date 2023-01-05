package lang.proteus.syntax.parser

import lang.proteus.diagnostics.Diagnosable
import lang.proteus.diagnostics.Diagnostics
import lang.proteus.syntax.lexer.Lexer
import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.Token

class SyntaxTree(
    val root: ExpressionSyntax,
    private val endOfFileToken: SyntaxToken<*>,
    override val diagnostics: Diagnostics
) : Diagnosable {

    companion object {
        fun parse(text: String, verbose: Boolean = false): SyntaxTree {
            val parser = Parser(text, verbose = verbose)
            return parser.parse()
        }

        fun parseTokens(input: String): List<SyntaxToken<*>> {
            val lexer = Lexer(input)
            val tokens = mutableListOf<SyntaxToken<*>>()
            while (true) {
                val token = lexer.nextToken()
                if (token.token is Token.EndOfFile) {
                    break
                }
                tokens.add(token)
            }
            return tokens

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