package lang.proteus.syntax.lexer.token

internal sealed class UnaryAndBinaryOperator(literal: kotlin.String, precedence: Int) : Operator(
    isBinaryOperator = true,
    isUnaryOperator = true,
    literal = literal,
    precedence = precedence
)