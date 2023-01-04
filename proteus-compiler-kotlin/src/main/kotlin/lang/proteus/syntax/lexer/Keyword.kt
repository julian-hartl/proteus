package lang.proteus.syntax.lexer

sealed class Keyword(val literal: String) : Token() {

    companion object {

        val all
            get() = Keyword::class.sealedSubclasses
                .map { it.objectInstance!! }

        fun fromString(value: String): Keyword? {
            return all.find { it.literal == value }
        }

    }

    fun toSyntaxToken(position: Int): SyntaxToken<Keyword> {
        return super.toSyntaxToken(position, literal, null) as SyntaxToken<Keyword>
    }


    internal object True : Keyword("true")

    internal object False : Keyword("false")
}

