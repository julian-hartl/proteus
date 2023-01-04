package lang.proteus.syntax.lexer

sealed class Token {
    object EndOfFile : Token()
    object Identifier : Token()
    object Whitespace : Token()
    object Number : Token()
    object Bad : Token()

    object Type : Token()

    fun toSyntaxToken(position: Int, literal: String, value: Any? = null): SyntaxToken<Token> {
        return SyntaxToken(this, position, literal, value)
    }

    override fun toString(): String {
        return this::class.simpleName!!
    }
}