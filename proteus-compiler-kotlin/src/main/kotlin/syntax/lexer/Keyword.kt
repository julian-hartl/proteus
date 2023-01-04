package syntax.lexer

sealed class Keyword(val literal: String) : Token() {

    companion object {

        val all
            get() = Keyword::class.sealedSubclasses
                .map { it.objectInstance!! }

        fun fromString(value: String): Keyword? {
            return all.find { it.literal == value }
        }

    }

    fun toSyntaxToken(position: Int, value: Any?): SyntaxToken<Keyword> {
        return super.toSyntaxToken(position, literal, value) as SyntaxToken<Keyword>
    }


    internal object True : Keyword("true")

    internal object False : Keyword("false")
}

