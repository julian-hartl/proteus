package lang.proteus.syntax.lexer.token

sealed class UnaryOperator(literal: String, precedence: Int) : Operator(
    isBinaryOperator = false,
    isUnaryOperator = true,
    literal = literal,
    precedence = precedence
)