package lang.proteus.syntax.lexer.token

import lang.proteus.syntax.lexer.SyntaxToken

sealed class Operator(
    override val literal: String,
    val precedence: Int,
    val isUnaryOperator: Boolean = false,
    val isBinaryOperator: Boolean = false,
    val isAssignmentOperator: Boolean = false,
) : Token() {

    fun toSyntaxToken(position: Int): SyntaxToken<Operator> {
        return super.toSyntaxToken(position, literal, null) as SyntaxToken<Operator>
    }

    fun unaryPrecedence(): Int {
        return if (isUnaryOperator) 100 else 0
    }

    object Plus : UnaryAndBinaryOperator("+", 1)

    object Minus : UnaryAndBinaryOperator("-", 1)

    object Asterisk : BinaryOperator("*", 2)

    object Slash : BinaryOperator("/", 2)
    object DoubleCircumflex : BinaryOperator("^^", 3)
    object DoubleGreaterThan : BinaryOperator(">>", 3)
    object DoubleLessThan : BinaryOperator("<<", 3)

    object Ampersand : BinaryOperator("&", 3)

    object Pipe : BinaryOperator("|", 3)
    object Circumflex : BinaryOperator("^", 3)

    object OpenParenthesis : BinaryOperator("(", 0)

    object CloseParenthesis : BinaryOperator(")", 0)

    object Not : UnaryOperator("not", 3)

    object And : BinaryOperator("and", 2)
    object DoubleEquals : BinaryOperator("==", 1)
    object NotEquals : BinaryOperator("!=", 1)

    object LessThan : BinaryOperator("<", 1)
    object GreaterThan : BinaryOperator(">", 1)
    object GreaterThanEquals : BinaryOperator(">=", 1)
    object LessThanEquals : BinaryOperator("<=", 1)

    object Or : BinaryOperator("or", 2)
    object Xor : BinaryOperator("xor", 2)
    object Is : BinaryOperator("is", 4)

    object Equals : AssignmentOperator("=", 1)

    object TypeOf : UnaryOperator("typeof", 4)

}






