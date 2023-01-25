package lang.proteus.syntax.lexer.token_lexers

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.parser.SyntaxTree

internal object NumberTokenLexer : TokenLexer() {
    override fun match(current: Char): Boolean {
        return current.isDigit()
    }

    override fun submit(start: Int, position: Int, literal: String, syntaxTree: SyntaxTree): TokenLexerResult {
        val token = SyntaxToken.numberToken(start, literal, syntaxTree)
        return TokenLexerResult(token, literal.length)
    }

}