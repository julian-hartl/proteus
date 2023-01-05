package lang.proteus.syntax.lexer.token_lexers

import lang.proteus.syntax.lexer.Operators
import lang.proteus.syntax.lexer.SyntaxToken

internal object NonLetterTokenLexer : TokenLexer() {

    private val SUPPORTED_NON_LETTER_CHARACTER = listOf(
        '(', ')', '=', '+', '-', '*', '/', '^', '!', '<', '>', '&', '|', '^',
    )

    override fun match(current: Char): Boolean {
        return SUPPORTED_NON_LETTER_CHARACTER.contains(current)
    }

    override fun submit(start: Int, position: Int, literal: String): SyntaxToken<*>? {
        var end = literal.length
        var currentLiteral = literal
        while (end > 0) {
            val operator = Operators.fromLiteral(currentLiteral)
            if (operator != null) {
                return operator.toSyntaxToken(start)
            }
            currentLiteral = currentLiteral.substring(0, --end)
        }
        return null
    }

}