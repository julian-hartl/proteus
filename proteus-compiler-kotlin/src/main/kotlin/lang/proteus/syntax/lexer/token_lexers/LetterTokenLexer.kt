package lang.proteus.syntax.lexer.token_lexers

import lang.proteus.binding.ProteusType
import lang.proteus.syntax.lexer.Keyword
import lang.proteus.syntax.lexer.Operators
import lang.proteus.syntax.lexer.SyntaxToken

internal object LetterTokenLexer : TokenLexer() {
    override fun match(current: Char): Boolean {
        return current.isLetter()
    }

    override fun submit(start: Int, position: Int, literal: String): SyntaxToken<*> {
        val type = ProteusType.fromName(literal)
        if (type != null) {
            return SyntaxToken.typeToken(start, literal, type)
        }
        val operator = Operators.fromLiteral(literal)
        if (operator != null) {
            return operator.toSyntaxToken(start)
        }
        val keyword = Keyword.fromLiteral(literal)
        if (keyword != null) {
            return keyword.toSyntaxToken(start)
        }
        return SyntaxToken.identifierToken(start, literal)
    }

}