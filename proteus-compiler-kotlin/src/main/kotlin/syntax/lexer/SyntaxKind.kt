package syntax.lexer

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
    TrueKeyword,
    FalseKeyword,
    UnaryExpression,
    IdentifierToken;

    companion object {
        fun fromOperator(operator: Operator): SyntaxKind {
            return operator.syntaxKind
        }

        fun fromKeyword(keyword: Keyword): SyntaxKind {
            return when (keyword) {
                Keyword.True -> TrueKeyword
                Keyword.False -> FalseKeyword
                else -> {
                    throw Exception("Unexpected keyword $keyword")
                }
            }
        }

    }

    fun toOperator(): Operator {
        return Operator.fromSyntaxKind(this)
    }
}
