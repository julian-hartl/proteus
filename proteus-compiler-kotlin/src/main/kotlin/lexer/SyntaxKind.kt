package lexer

enum class SyntaxKind {
    NumberToken,
    PlusToken,
    MinusToken,
    AsteriskToken,
    SlashToken,
    OpenParenthesisToken,
    CloseParenthesisToken,
    EndOfFileToken,
    WhiteSpaceToken,
    BadToken,
    ParenthesizedExpression,
    LiteralExpression,
    AmpersandToken,
    PipeToken,
    BinaryExpression,
    EqualityToken,
    UnaryExpression;

    companion object {
        fun fromOperator(operator: Operator): SyntaxKind {
            return operator.syntaxKind
        }

    }

    fun toOperator(): Operator {
        return Operator.fromSyntaxKind(this)
    }
}
