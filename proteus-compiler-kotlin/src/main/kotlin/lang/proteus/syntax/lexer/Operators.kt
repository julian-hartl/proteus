package lang.proteus.syntax.lexer

object Operators {
    val allOperators: List<Operator>
        get() = Tokens.allTokens.filterIsInstance<Operator>()

    val assignmentOperators: List<Operator>
        get() = allOperators.filter { it.isAssignmentOperator }

    val binaryOperators: List<Operator>
        get() = allOperators.filter { it.isBinaryOperator }

    val unaryOperators: List<Operator>
        get() = allOperators.filter { it.isUnaryOperator }

    val maxOperatorLength: Int
        get() = allOperators.maxOfOrNull { it.literal.length } ?: 0

    val maxNonLetterOperatorLength: Int
        get() = allOperators.filter {
            !it.literal[0].isLetter()
        }.maxOfOrNull { it.literal.length } ?: 0

    fun isOperator(literal: String): Boolean {
        return fromLiteral(literal) != null
    }

    fun fromLiteral(literal: String): Operator? {
        return allOperators.find { it.literal == literal }
    }
}