package syntax.lexer

sealed class Operator(
    val literal: String,
    val precedence: Int
) : Token() {
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

    }

    fun toSyntaxToken(position: Int): SyntaxToken<Operator> {
        return super.toSyntaxToken(position, literal, null) as SyntaxToken<Operator>
    }

    fun unaryPrecedence(): Int {
        return when (this) {
            is Plus -> 100
            is Minus -> 100
            is Not -> 100
            else -> 0
        }
    }

    object Plus : Operator("+", 1)

    object Minus : Operator("-", 1)

    object Asterisk : Operator("*", 2)

    object Slash : Operator("/", 2)
    object DoubleCircumflex : Operator("^^", 3)
    object DoubleGreaterThan : Operator(">>", 3)
    object DoubleLessThan : Operator("<<", 3)

    object Ampersand : Operator("&", 3)

    object Pipe : Operator("|", 3)
    object Circumflex : Operator("^", 3)

    object OpenParenthesis : Operator("(", 0)

    object CloseParenthesis : Operator(")", 0)

    object Not : Operator("not", 3)

    object And : Operator("and", 2)
    object DoubleEquals : Operator("==", 1)
    object NotEquals : Operator("!=", 1)

    object LessThan : Operator("<", 1)
    object GreaterThan : Operator(">", 1)
    object GreaterThanEquals : Operator(">=", 1)
    object LessThanEquals : Operator("<=", 1)

    object Or : Operator("or", 2)
    object Xor : Operator("xor", 2)
}







