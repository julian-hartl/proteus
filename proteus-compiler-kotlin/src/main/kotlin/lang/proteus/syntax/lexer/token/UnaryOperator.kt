package lang.proteus.syntax.lexer.token

internal sealed class UnaryOperator(literal: kotlin.String, precedence: Int) : Operator(
    isBinaryOperator = false,
    isUnaryOperator = true,
    literal = literal,
    precedence = precedence
)