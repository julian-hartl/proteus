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
    BitwiseAndToken,
    BinaryExpression,
    EqualityToken;
    companion object {

    }
}

val BOOLEAN_OPERATORS: List<SyntaxKind> = listOf(SyntaxKind.EqualityToken)

fun SyntaxKind.isBooleanOperator(): Boolean {
    return BOOLEAN_OPERATORS.contains(this)
}

fun SyntaxKind.isLowPriorityBinaryOperator(): Boolean {
    return this == SyntaxKind.PlusToken || this == SyntaxKind.MinusToken
}

fun SyntaxKind.isHighPriorityBinaryOperator(): Boolean {
    return this == SyntaxKind.AsteriskToken || this == SyntaxKind.SlashToken || this == SyntaxKind.BitwiseAndToken
}

fun SyntaxKind.isOperator(): Boolean {
    return isBooleanOperator() || isLowPriorityBinaryOperator() || isHighPriorityBinaryOperator()
}

fun SyntaxKind.Companion.fromOperator(operator: String): SyntaxKind {
    return when (operator) {
        "+" -> SyntaxKind.PlusToken
        "-" -> SyntaxKind.MinusToken
        "*" -> SyntaxKind.AsteriskToken
        "/" -> SyntaxKind.SlashToken
        else -> {
            throw Exception("Unexpected operator: $operator")
        }
    }
}