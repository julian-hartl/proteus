package lang.proteus.syntax.lexer

internal sealed class Token(open val literal: String? = null) {
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
    object QuotationMark: Token("\"")

    object SingleQuote : Token("'")


    fun toSyntaxToken(position: Int, literal: String, value: Any? = null): SyntaxToken<Token> {
        return SyntaxToken(this, position, literal, value)
    }

    override fun toString(): String {
        return this::class.simpleName!!
    }
}