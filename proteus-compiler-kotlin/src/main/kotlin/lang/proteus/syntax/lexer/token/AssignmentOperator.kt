package lang.proteus.syntax.lexer.token

sealed class AssignmentOperator(literal: kotlin.String, precedence: Int) : Operator(
    isBinaryOperator = false,
    isUnaryOperator = false,
    isAssignmentOperator = true,
    literal = literal,
    precedence = precedence
)