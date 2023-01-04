package lang.proteus.syntax.lexer

import lang.proteus.binding.BoundType
import lang.proteus.diagnostics.TextSpan
import lang.proteus.syntax.parser.SyntaxNode

class SyntaxToken<T : Token>(
    override val token: T,
    var position: Int,
    val literal: String,
    val value: Any?
) : SyntaxNode() {
    companion object {


        fun typeToken(position: Int, literal: String, type: BoundType): SyntaxToken<Token> {
            return Token.Type.toSyntaxToken(position, literal, value = type)
        }

        fun keywordToken(position: Int, literal: String): SyntaxToken<*> {
            val keyword = Keyword.fromString(literal) ?: return identifierToken(position, literal)
            return keyword.toSyntaxToken(position)
        }

        fun identifierToken(position: Int, literal: String): SyntaxToken<Token.Identifier> {
            return Token.Identifier.toSyntaxToken(position, literal) as SyntaxToken<Token.Identifier>
        }

        fun numberToken(position: Int, literal: String): SyntaxToken<Token.Number> {
            return Token.Number.toSyntaxToken(position, literal, literal.toIntOrNull()) as SyntaxToken<Token.Number>
        }

        fun whiteSpaceToken(position: Int, literal: String): SyntaxToken<Token.Whitespace> {
            return Token.Whitespace.toSyntaxToken(position, literal, null) as SyntaxToken<Token.Whitespace>
        }

        fun operator(position: Int, operatorLiteral: String): SyntaxToken<Operator>? {
            val operator = Operator.fromLiteral(operatorLiteral) ?: return null
            return operator.toSyntaxToken(position)
        }

        fun badToken(position: Int, literal: String): SyntaxToken<Token.Bad> {
            return Token.Bad.toSyntaxToken(position, literal) as SyntaxToken<Token.Bad>
        }
    }

    override fun getChildren(): Iterator<SyntaxNode> {
        return iterator { }
    }


    val span: TextSpan
        get() = TextSpan(position, literal)


}