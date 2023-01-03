package lexer

import parser.SyntaxNode

class SyntaxToken<T>(
    override val kind: SyntaxKind,
    var position: Int,
    val literal: String?,
    val value: T
) : SyntaxNode() {
    companion object {


        fun bitwiseAndToken(position: Int): SyntaxToken<*> {
            return SyntaxToken(SyntaxKind.BitwiseAndToken, position, "&", null)
        }

        fun equalityToken(position: Int): SyntaxToken<Boolean> {
            return SyntaxToken(SyntaxKind.EqualityToken, position, "==", true)
        }

        fun numberToken(position: Int, literal: String): SyntaxToken<Int?> {
            return SyntaxToken(SyntaxKind.NumberToken, position, literal, literal.toIntOrNull())
        }

        fun whiteSpaceToken(position: Int, literal: String): SyntaxToken<Nothing?> {
            return SyntaxToken(SyntaxKind.WhiteSpaceToken, position, literal, null)
        }

        fun fromOperator(position: Int, operator: String): SyntaxToken<Nothing?> {
            return SyntaxToken(SyntaxKind.fromOperator(operator), position, operator, null)
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