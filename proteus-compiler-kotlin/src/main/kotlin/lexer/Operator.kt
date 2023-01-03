package lexer

sealed class Operator(val syntaxKind: SyntaxKind, val literal: String) {
    companion object {
        val all = listOf(
            PlusOperator(),
            MinusOperator(),
            AsteriskOperator(),
            SlashOperator(),
            AmpersandOperator(),
            EqualityOperator(),
            OpenParenthesisOperator(),
            CloseParenthesisOperator()
        )

        fun isOperator(literal: String): Boolean {
            return fromLiteral(literal) != null
        }

        fun fromLiteral(literal: String): Operator? {
            return all.find { it.literal == literal }
        }

        fun fromLiteralOrThrow(literal: String): Operator {
            return fromLiteral(literal) ?: throw IllegalArgumentException("Unknown operator: $literal")
        }
    }
}

class PlusOperator : Operator(SyntaxKind.PlusToken, "+")

class MinusOperator : Operator(SyntaxKind.MinusToken, "-")

class AsteriskOperator : Operator(SyntaxKind.AsteriskToken, "*")

class SlashOperator : Operator(SyntaxKind.SlashToken, "/")

class AmpersandOperator : Operator(SyntaxKind.AmpersandToken, "&")

class OpenParenthesisOperator : Operator(SyntaxKind.OpenParenthesisToken, "(")

class CloseParenthesisOperator : Operator(SyntaxKind.CloseParenthesisToken, ")")

class EqualityOperator : Operator(SyntaxKind.EqualityToken, "==")


