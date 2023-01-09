package lang.proteus.syntax.lexer.token_lexers

import lang.proteus.syntax.lexer.SyntaxToken

internal object WhiteSpaceTokenLexer : TokenLexer() {
    override fun match(current: Char): Boolean {
        return current.isWhitespace()
    }

    override fun submit(start: Int, position: Int, literal: String): TokenLexerResult {
        val whiteSpaceToken = SyntaxToken.whiteSpaceToken(start, literal)
        return TokenLexerResult(whiteSpaceToken, literal.length)
    }

}