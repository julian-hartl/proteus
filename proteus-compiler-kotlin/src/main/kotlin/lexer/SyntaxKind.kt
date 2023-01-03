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
    EqualityToken;

    companion object {
        fun fromOperator(operator: Operator): SyntaxKind {
            return operator.syntaxKind
        }

    }
}
