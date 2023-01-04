package lang.proteus.binding

import lang.proteus.syntax.lexer.Operator

internal sealed class BoundUnaryOperator(
    val operator: Operator,
    val operandType: BoundType,
    val resultType: BoundType
) {

    constructor(operator: Operator, type: BoundType) : this(
        operator,
        type,
        type
    )

    companion object {
        private val operators = BoundUnaryOperator::class.sealedSubclasses.map { it.objectInstance!! }

        fun bind(operator: Operator, operandType: BoundType): BoundUnaryOperator? {
            return operators.firstOrNull { it.operator == operator && it.operandType.isAssignableTo(operandType) }
        }
    }

    object BoundUnaryNotOperator : BoundUnaryOperator(Operator.Not, BoundType.Boolean)
    object BoundUnaryIdentityOperator : BoundUnaryOperator(Operator.Plus, BoundType.Int)
    object BoundUnaryNegationOperator : BoundUnaryOperator(Operator.Minus, BoundType.Int)

}