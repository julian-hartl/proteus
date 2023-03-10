package lang.proteus.syntax.lexer.token_lexers

import lang.proteus.diagnostics.Diagnostic
import lang.proteus.syntax.parser.SyntaxTree

internal sealed class TokenLexer(internal val maxLength: Int? = null) {
    abstract fun match(current: Char): Boolean

    abstract fun submit(start: Int, position: Int, literal: String, syntaxTree: SyntaxTree): TokenLexerResult?

    open fun getDiagnostics(): List<Diagnostic> {
        return emptyList()
    }
}