package lang.proteus.syntax.parser

import lang.proteus.diagnostics.Diagnosable
import lang.proteus.diagnostics.Diagnostics
import lang.proteus.syntax.lexer.Lexer
import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Token
import lang.proteus.text.SourceText
import java.io.File

internal data class ParseHandlerResult(val root: CompilationUnitSyntax, val diagnostics: Diagnostics)

internal typealias ParseHandler = (SyntaxTree) -> ParseHandlerResult

internal data class ParseTokenResult(
    val root: CompilationUnitSyntax,
    val diagnostics: Diagnostics,
    val tokens: List<SyntaxToken<*>>,
)

internal class SyntaxTree(
    val sourceText: SourceText,
    parseHandler: ParseHandler,
) : Diagnosable {
    internal var root: CompilationUnitSyntax;

    override lateinit var diagnostics: Diagnostics;

    val id: Int = currentId++

    init {
        val parseResult = parseHandler(this)
        root = parseResult.root
        diagnostics = parseResult.diagnostics
    }


    companion object {

        private var currentId = 0

        fun reset() {
            currentId = 0
        }

        fun load(fileName: String): SyntaxTree {
            val content = File(fileName).readText()
            val sourceText = SourceText(content, fileName)
            return parse(sourceText)
        }

        fun parse(text: String): SyntaxTree {
            val sourceText = SourceText.from(text)
            return parse(sourceText)
        }

        fun parse(sourceText: SourceText): SyntaxTree {
            return SyntaxTree(sourceText) {
                parse(it)
            }
        }

        fun parseTokens(text: String): List<SyntaxToken<*>> {
            val sourceText = SourceText.from(text)
            return parseTokens(sourceText)
        }

        fun parseTokens(sourceText: SourceText): List<SyntaxToken<*>> {
            var result: ParseTokenResult? = null
            SyntaxTree(sourceText) {
                result = parseTokens(it)
                return@SyntaxTree ParseHandlerResult(result!!.root, result!!.diagnostics)
            }
            return result!!.tokens
        }

        fun parseTokens(syntaxTree: SyntaxTree): ParseTokenResult {
            var root: CompilationUnitSyntax? = null
            val lexer = Lexer(syntaxTree)
            val tokens = mutableListOf<SyntaxToken<*>>()
            while (true) {
                val token = lexer.nextToken()
                if (token.token is Token.EndOfFile) {
                    root = CompilationUnitSyntax(emptyList(), token as SyntaxToken<Token.EndOfFile>, syntaxTree)
                    break
                }
                tokens.add(token)
            }
            val diagnostics = lexer.diagnosticsBag.diagnostics
            return ParseTokenResult(root!!, diagnostics, tokens)
        }

        private fun parse(syntaxTree: SyntaxTree): ParseHandlerResult {
            val parser = Parser(syntaxTree)
            val compilationUnit = parser.parseCompilationUnit()
            return ParseHandlerResult(compilationUnit, parser.diagnostics)
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