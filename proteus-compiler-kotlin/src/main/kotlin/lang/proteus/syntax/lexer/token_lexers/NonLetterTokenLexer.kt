package lang.proteus.syntax.lexer.token_lexers

import lang.proteus.syntax.lexer.Grammar
import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.Tokens

internal object NonLetterTokenLexer : TokenLexer() {


    override fun match(current: Char): Boolean {
        return Grammar.allowedNonAlphanumericCharacters.contains(current)
    }

    override fun submit(start: Int, position: Int, literal: String): SyntaxToken<*>? {
        var end = literal.length
        var currentLiteral = literal
        while (end > 0) {
            val token = Tokens.fromLiteral(currentLiteral)
            if (token != null) {
                return token.toSyntaxToken(start, literal, null)
            }
            currentLiteral = currentLiteral.substring(0, --end)
        }
        return null
    }

}