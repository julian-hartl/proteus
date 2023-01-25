package lang.proteus.syntax.lexer.token_lexers

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.parser.SyntaxTree

internal object WhiteSpaceTokenLexer : TokenLexer() {
    override fun match(current: Char): Boolean {
        return current.isWhitespace()
    }

    override fun submit(start: Int, position: Int, literal: String,  syntaxTree: SyntaxTree): TokenLexerResult {
        val whiteSpaceToken =
            SyntaxToken.whiteSpaceToken(start, literal, syntaxTree)
        return TokenLexerResult(whiteSpaceToken, literal.length)
    }

}