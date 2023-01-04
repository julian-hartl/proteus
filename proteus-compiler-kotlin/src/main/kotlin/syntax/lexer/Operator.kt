package syntax.lexer

sealed class Operator(
    val syntaxKind: SyntaxKind,
    val literal: String,
    val operatorType: OperatorType,
    val precedence: Int
) {
    companion object {
        val all
            get() = Operator::class.sealedSubclasses
                .map { it.objectInstance!! }


        val maxOperatorLength: Int
            get() = all.maxOfOrNull { it.literal.length } ?: 0

        fun isOperator(literal: String): Boolean {
            return fromLiteral(literal) != null
        }

        fun fromLiteral(literal: String): Operator? {
            return all.find { it.literal == literal }
        }

        fun fromLiteralOrThrow(literal: String): Operator {
            return fromLiteral(literal) ?: throw IllegalArgumentException("Unknown operator: $literal")
        }

        fun fromSyntaxKind(syntaxKind: SyntaxKind): Operator {
            return all.find { it.syntaxKind == syntaxKind }
                ?: throw IllegalArgumentException("Unknown syntax kind: $syntaxKind")
        }
    }

    fun unaryPrecedence(): Int {
        return when (this) {
            is PlusOperator -> 100
            is MinusOperator -> 100
            is NotOperator -> 100
            else -> 0
        }
    }
}

object PlusOperator : Operator(SyntaxKind.PlusToken, "+", OperatorType.Arithmetic, 1)

object MinusOperator : Operator(SyntaxKind.MinusToken, "-", OperatorType.Arithmetic, 1)

object AsteriskOperator : Operator(SyntaxKind.AsteriskToken, "*", OperatorType.Arithmetic, 2)

object SlashOperator : Operator(SyntaxKind.SlashToken, "/", OperatorType.Arithmetic, 2)
object DoubleCircumflexOperator  : Operator(SyntaxKind.DoubleCircumflexToken, "^^", OperatorType.Arithmetic, 3)

object AmpersandOperator : Operator(SyntaxKind.AmpersandToken, "&", OperatorType.Bitwise, 3)

object PipeOperator : Operator(SyntaxKind.PipeToken, "|", OperatorType.Bitwise, 3)

object OpenParenthesisOperator : Operator(SyntaxKind.OpenParenthesisToken, "(", OperatorType.Other, 0)

object CloseParenthesisOperator : Operator(SyntaxKind.CloseParenthesisToken, ")", OperatorType.Other, 0)

object NotOperator : Operator(SyntaxKind.NotToken, "not", OperatorType.Logical, 3)

object AndOperator : Operator(SyntaxKind.AndToken, "and", OperatorType.Logical, 2)
object EqualityOperator : Operator(SyntaxKind.EqualityToken, "==", OperatorType.Relational, 1)
object NotEqualityOperator : Operator(SyntaxKind.NotEqualityToken, "!=", OperatorType.Relational, 1)

object LessThanOperator : Operator(SyntaxKind.LessThanToken, "<", OperatorType.Relational, 1)
object GreaterThanOperator : Operator(SyntaxKind.GreaterThanToken, ">", OperatorType.Relational, 1)
object GreaterThanOrEqualsOperator : Operator(SyntaxKind.GreaterThanOrEqualsToken, ">=", OperatorType.Relational, 1)
object LessThanOrEqualsToken : Operator(SyntaxKind.LessThanOrEqualsToken, "<=", OperatorType.Relational, 1)

object OrOperator : Operator(SyntaxKind.OrToken, "or", OperatorType.Logical, 2)
object XorOperator : Operator(SyntaxKind.XorToken, "xor", OperatorType.Logical, 2)




