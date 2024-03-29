package lang.proteus.syntax.lexer.token

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.parser.SyntaxTree

internal sealed class Operator(
    override val literal: kotlin.String,
    val precedence: Int,
    val isUnaryOperator: Boolean = false,
    val isBinaryOperator: Boolean = false,
    val isAssignmentOperator: Boolean = false,
    val isComparisonOperator: Boolean = false,
    val unaryPrecedence: Int? = null,
) : Token() {

    fun toSyntaxToken(position: Int, syntaxTree: SyntaxTree): SyntaxToken<Operator> {
        return super.toSyntaxToken(position, literal, null, syntaxTree) as SyntaxToken<Operator>
    }

    fun unaryPrecedence(): Int {
        return if (isUnaryOperator) this.unaryPrecedence ?: this.precedence else 0
    }

    object Plus : UnaryAndBinaryOperator("+", 1,1)

    object Minus : UnaryAndBinaryOperator("-", 1,1)

    object Asterisk : UnaryAndBinaryOperator("*", 2, 1)

    object Slash : BinaryOperator("/", 2)
    object Percent : BinaryOperator("%", 2)
    object DoubleAsterisk : BinaryOperator("**", 3)
    object DoubleGreaterThan : BinaryOperator(">>", 3)
    object DoubleLessThan : BinaryOperator("<<", 3)

    object Ampersand : BinaryOperator("&", 3)


    object Pipe : BinaryOperator("|", 3)
    object Circumflex : BinaryOperator("^", 3)

    object OpenParenthesis : BinaryOperator("(", 0)

    object CloseParenthesis : BinaryOperator(")", 0)

    object Not : UnaryOperator("not", 3)

    object And : BinaryOperator("and", 2)
    object DoubleEquals : ComparisonOperator("==", 1)
    object NotEquals : ComparisonOperator("!=", 1)

    object LessThan : ComparisonOperator("<", 1)
    object GreaterThan : ComparisonOperator(">", 1)
    object GreaterThanEquals : ComparisonOperator(">=", 1)
    object LessThanEquals : ComparisonOperator("<=", 1)

    object Or : BinaryOperator("or", 2)
    object Xor : BinaryOperator("xor", 2)
    object Is : BinaryOperator("is", 4)

    object Equals : AssignmentOperator("=", 1)

    object PlusEquals : AssignmentOperator("+=", 1)

    object MinusEquals : AssignmentOperator("-=", 1)


    object TypeOf : UnaryOperator("typeof", 4)

}







