package lang.proteus.syntax.lexer.token

sealed class BinaryOperator(literal: String, precedence: Int) : Operator(
    isBinaryOperator = true,
    isUnaryOperator = false,
    literal = literal,
    precedence = precedence
)