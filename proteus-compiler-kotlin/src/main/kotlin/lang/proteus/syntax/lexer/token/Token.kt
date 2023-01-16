package lang.proteus.syntax.lexer.token

import lang.proteus.syntax.lexer.SyntaxToken

sealed class Token(open val literal: kotlin.String? = null) {
    object EndOfFile : Token()
    object Identifier : Token()
    object Whitespace : Token()
    object Number : Token()
    object Bad : Token()

    object Type : Token()

    object Expression : Token()
    object Statement : Token()

    object OpenBrace : Token("{")

    object CloseBrace : Token("}")
    object SingleQuote : Token("'")
    object ElseClause : Token()

    object SemiColon : Token(";")

    object String : Token()

    fun toSyntaxToken(position: Int, literal: kotlin.String, value: Any? = null): SyntaxToken<Token> {
        return SyntaxToken(this, position, literal, value, usePositionBasedSpan = true)
    }

    override fun toString(): kotlin.String {
        return this::class.simpleName!!
    }
}