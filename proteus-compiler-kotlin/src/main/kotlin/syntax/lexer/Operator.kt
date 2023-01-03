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

        val logical: List<Operator>
            get() = filterByType(OperatorType.Logical)

        fun filterByType(operatorType: OperatorType): List<Operator> {
            return all.filter { it.operatorType == operatorType }
        }

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
            return all.find { it.syntaxKind == syntaxKind } ?: throw IllegalArgumentException("Unknown syntax kind: $syntaxKind")
        }
    }

    fun unaryPrecedence(): Int {
        return when (this) {
            is PlusOperator -> 100
            is MinusOperator -> 100
            else -> 0
        }
    }
}

object PlusOperator : Operator(SyntaxKind.PlusToken, "+", OperatorType.Arithmetic, 1)

object MinusOperator : Operator(SyntaxKind.MinusToken, "-", OperatorType.Arithmetic, 1)

object AsteriskOperator : Operator(SyntaxKind.AsteriskToken, "*", OperatorType.Arithmetic, 2)

object SlashOperator : Operator(SyntaxKind.SlashToken, "/", OperatorType.Arithmetic, 2)

object AmpersandOperator : Operator(SyntaxKind.AmpersandToken, "&", OperatorType.Bitwise, 3)

object PipeOperator : Operator(SyntaxKind.PipeToken, "|", OperatorType.Bitwise, 3)

object OpenParenthesisOperator : Operator(SyntaxKind.OpenParenthesisToken, "(", OperatorType.Other, 0)

object CloseParenthesisOperator : Operator(SyntaxKind.CloseParenthesisToken, ")", OperatorType.Other, 0)



