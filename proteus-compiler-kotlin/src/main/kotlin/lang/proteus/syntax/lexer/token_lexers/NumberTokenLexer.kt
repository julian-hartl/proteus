package lang.proteus.syntax.lexer.token_lexers

import lang.proteus.syntax.lexer.SyntaxToken

internal object NumberTokenLexer : TokenLexer() {
    override fun match(current: Char): Boolean {
        return current.isDigit()
    }

    override fun submit(start: Int, position: Int, literal: String): TokenLexerResult {
        val token = SyntaxToken.numberToken(start, literal)
        return TokenLexerResult(token, literal.length)
    }

}