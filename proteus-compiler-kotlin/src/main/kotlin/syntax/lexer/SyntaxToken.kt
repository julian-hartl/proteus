package syntax.lexer

import syntax.parser.SyntaxNode

class SyntaxToken<T>(
    override val kind: SyntaxKind,
    var position: Int,
    val literal: String,
    val value: T
) : SyntaxNode() {
    companion object {


        fun keywordToken(position: Int, literal: String): SyntaxToken<*> {
            val keyword = Keyword.fromString(literal) ?: return identifierToken(position, literal)
            return SyntaxToken(
                SyntaxKind.fromKeyword(keyword),
                position,
                literal,
                keyword
            )
        }

        fun identifierToken(position: Int, literal: String): SyntaxToken<*> {
            return SyntaxToken(
                SyntaxKind.IdentifierToken,
                position,
                literal,
                null
            )
        }

        fun numberToken(position: Int, literal: String): SyntaxToken<Int?> {
            return SyntaxToken(SyntaxKind.NumberToken, position, literal, literal.toIntOrNull())
        }

        fun whiteSpaceToken(position: Int, literal: String): SyntaxToken<Nothing?> {
            return SyntaxToken(SyntaxKind.WhiteSpaceToken, position, literal, null)
        }

        fun operator(position: Int, operator: String): SyntaxToken<Nothing?> {
            return SyntaxToken(SyntaxKind.fromOperator(Operator.fromLiteralOrThrow(operator)), position, operator, null)
        }

        fun badToken(position: Int, literal: String): SyntaxToken<Nothing?> {
            return SyntaxToken(SyntaxKind.BadToken, position, literal, null)
        }
    }

    override fun getChildren(): Iterator<SyntaxNode> {
        return iterator { }
    }

    override fun toString(): String {
        return "SyntaxToken(kind=$kind, position=$position, literal=$literal, value=$value)"
    }


}