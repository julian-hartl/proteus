package lang.proteus.syntax.parser

import lang.proteus.diagnostics.Diagnosable
import lang.proteus.diagnostics.Diagnostics
import lang.proteus.syntax.lexer.Lexer
import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Token
import lang.proteus.text.SourceText

internal class SyntaxTree private constructor(
) : Diagnosable {
    internal lateinit var root: CompilationUnitSyntax;

    override lateinit var diagnostics: Diagnostics;

    constructor(text: SourceText) : this() {
        val parser = Parser(text)
        root = parser.parseCompilationUnit()
        diagnostics = parser.diagnostics
    }


    companion object {
        fun parse(text: String): SyntaxTree {
            val sourceText = SourceText.from(text)
            return parse(sourceText)
        }

        fun parse(sourceText: SourceText): SyntaxTree {
            return SyntaxTree(sourceText)
        }

        fun parseTokens(text: String): List<SyntaxToken<*>> {
            val sourceText = SourceText.from(text)
            return parseTokens(sourceText)
        }

        fun parseTokens(sourceText: SourceText): List<SyntaxToken<*>> {
            val lexer = Lexer(sourceText)
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