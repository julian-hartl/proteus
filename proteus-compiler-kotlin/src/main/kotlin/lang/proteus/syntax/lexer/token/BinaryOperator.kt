package lang.proteus.syntax.lexer.token

sealed class BinaryOperator(literal: kotlin.String, precedence: Int) : Operator(
    isBinaryOperator = true,
    isUnaryOperator = false,
    literal = literal,
    precedence = precedence
)