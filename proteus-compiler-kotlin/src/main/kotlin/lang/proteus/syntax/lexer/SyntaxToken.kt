package lang.proteus.syntax.lexer

import lang.proteus.binding.ProteusType
import lang.proteus.diagnostics.TextSpan
import lang.proteus.syntax.parser.SyntaxNode

internal class SyntaxToken<T : Token>(
    override val token: T,
    var position: Int,
    val literal: String,
    val value: Any?,
) : SyntaxNode() {
    companion object {


        fun endOfFile(position: Int): SyntaxToken<Token.EndOfFile> {
            return SyntaxToken(Token.EndOfFile, position, "", null)
        }

        fun typeToken(position: Int, literal: String, type: ProteusType): SyntaxToken<Token> {
            return Token.Type.toSyntaxToken(position, literal, value = type)
        }

        fun keywordToken(position: Int, keyword: Keyword): SyntaxToken<*> {
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
            val operator = Operators.fromLiteral(operatorLiteral) ?: return null
            return operator.toSyntaxToken(position)
        }

        fun badToken(position: Int, literal: String): SyntaxToken<Token.Bad> {
            return Token.Bad.toSyntaxToken(position, literal) as SyntaxToken<Token.Bad>
        }
    }



}