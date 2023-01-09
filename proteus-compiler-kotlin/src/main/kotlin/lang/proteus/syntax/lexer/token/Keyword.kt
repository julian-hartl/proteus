package lang.proteus.syntax.lexer.token

import lang.proteus.syntax.lexer.SyntaxToken

internal sealed class Keyword(override val literal: String) : Token() {

    companion object {

        val all
            get() = Keyword::class.sealedSubclasses
                .map { it.objectInstance!! }

        fun fromLiteral(value: String): Keyword? {
            return all.find { it.literal == value }
        }

    }

    fun toSyntaxToken(position: Int): SyntaxToken<Keyword> {
        return super.toSyntaxToken(position, literal, null) as SyntaxToken<Keyword>
    }


    internal object True : Keyword("true")

    internal object False : Keyword("false")

    internal object Val : Keyword("val")

    internal object Var : Keyword("var")
}

