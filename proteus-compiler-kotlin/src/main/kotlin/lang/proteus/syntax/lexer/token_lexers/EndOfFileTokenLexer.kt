package lang.proteus.syntax.lexer.token_lexers

import lang.proteus.syntax.lexer.SyntaxToken

internal object EndOfFileTokenLexer : TokenLexer(maxLength = 1) {
    override fun match(current: Char): Boolean {
        return current == '\u0000'
    }

    override fun submit(start: Int, position: Int, literal: String): TokenLexerResult {
        val endOfFile = SyntaxToken.endOfFile(start)
        return TokenLexerResult(endOfFile, literal.length)
    }

}