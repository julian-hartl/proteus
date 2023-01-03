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
    return this == SyntaxKind.AsteriskToken || this == SyntaxKind.SlashToken || this == SyntaxKind.AmpersandToken
}

fun SyntaxKind.isOperator(): Boolean {
    return Operator.all.map { it.syntaxKind }.contains(this)
}

fun SyntaxKind.Companion.fromOperator(operator: Operator): SyntaxKind {
    return operator.syntaxKind
}