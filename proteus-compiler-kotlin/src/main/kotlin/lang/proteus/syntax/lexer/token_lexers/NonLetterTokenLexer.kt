package lang.proteus.syntax.lexer.token_lexers

import lang.proteus.syntax.lexer.Grammar
import lang.proteus.syntax.lexer.Tokens

internal object NonLetterTokenLexer : TokenLexer() {


    override fun match(current: Char): Boolean {
        return Grammar.allowedNonAlphanumericCharacters.contains(current)
    }

    override fun submit(start: Int, position: Int, literal: String): TokenLexerResult? {
        var end = literal.length
        var currentLiteral = literal
        while (end > 0) {
            val token = Tokens.fromLiteral(currentLiteral)
            if (token != null) {
                val syntaxToken = token.toSyntaxToken(start, literal, null)
                return TokenLexerResult(syntaxToken, end)
            }
            currentLiteral = currentLiteral.substring(0, --end)
        }
        return null
    }

}