package lang.proteus.syntax.lexer.token

internal sealed class ComparisonOperator(literal: kotlin.String, precedence: Int) : Operator(
    isBinaryOperator = true,
    isUnaryOperator = false,
    literal = literal,
    precedence = precedence,
    isComparisonOperator = true,
)