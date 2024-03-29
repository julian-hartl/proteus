package lang.proteus.syntax.lexer.token

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.parser.SyntaxTree

internal sealed class Keyword(override val literal: kotlin.String) : Token() {

    companion object {

        val all
            get() = Keyword::class.sealedSubclasses
                .map { it.objectInstance!! }

        fun fromLiteral(value: kotlin.String): Keyword? {
            return all.find { it.literal == value }
        }

    }

    fun toSyntaxToken(position: Int, syntaxTree: SyntaxTree): SyntaxToken<Keyword> {
        return super.toSyntaxToken(position, literal, null, syntaxTree) as SyntaxToken<Keyword>
    }


    internal object True : Keyword("true")

    internal object False : Keyword("false")

    internal object Let : Keyword("let")

    internal object Mut : Keyword("mut")
    internal object Const : Keyword("const")

    internal object If : Keyword("if")

    internal object Else : Keyword("else")
    internal object While : Keyword("while")
    internal object As : Keyword("as")

    internal object For : Keyword("for")
    internal object In : Keyword("in")

    internal object Until : Keyword("until")
    internal object Fn : Keyword("fn")
    internal object Break : Keyword("break")
    internal object Continue : Keyword("continue")
    internal object Return : Keyword("return")
    internal object Import : Keyword("import")
    internal object External : Keyword("external")
    internal object Struct : Keyword("struct")

}

