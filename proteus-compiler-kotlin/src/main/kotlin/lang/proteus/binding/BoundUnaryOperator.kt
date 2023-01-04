package lang.proteus.binding

import lang.proteus.syntax.lexer.Operator

internal sealed class BoundUnaryOperator(
    val operator: Operator,
    val operandType: ProteusType,
    val resultType: ProteusType
) {

    constructor(operator: Operator, type: ProteusType) : this(
        operator,
        type,
        type
    )

    companion object {
        private val operators = BoundUnaryOperator::class.sealedSubclasses.map { it.objectInstance!! }

        fun bind(operator: Operator, operandType: ProteusType): BoundUnaryOperator? {
            return operators.firstOrNull { it.operator == operator && it.operandType.isAssignableTo(operandType) }
        }
    }

    object BoundUnaryNotOperator : BoundUnaryOperator(Operator.Not, ProteusType.Boolean)
    object BoundUnaryIdentityOperator : BoundUnaryOperator(Operator.Plus, ProteusType.Int)
    object BoundUnaryNegationOperator : BoundUnaryOperator(Operator.Minus, ProteusType.Int)

}