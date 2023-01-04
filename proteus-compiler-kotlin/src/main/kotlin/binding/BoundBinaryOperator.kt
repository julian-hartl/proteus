package binding

import syntax.lexer.Operator


internal sealed class BoundBinaryOperator(
    val operator: Operator, val leftType: BoundType, val rightType: BoundType, val resultType: BoundType
) {


    constructor(operator: Operator, type: BoundType) : this(operator, type, type, type)

    constructor(operator: Operator, type: BoundType, resultType: BoundType) : this(
        operator,
        type,
        type,
        resultType
    )

    companion object {
        private val operators = BoundBinaryOperator::class.sealedSubclasses.map { it.objectInstance!! }

        fun bind(operator: Operator, leftType: BoundType, rightType: BoundType): BoundBinaryOperator? {
            if(rightType != leftType) return null
            return operators.firstOrNull {
                it.operator == operator && it.leftType.isAssignableTo(leftType) && it.rightType.isAssignableTo(
                    rightType
                )
            }
        }
    }

    object BoundAdditionBinaryOperator : BoundBinaryOperator(Operator.Plus, BoundType.Int)

    object BoundSubtractionBinaryOperator : BoundBinaryOperator(Operator.Minus, BoundType.Int)

    object BoundMultiplicationBinaryOperator : BoundBinaryOperator(Operator.Asterisk, BoundType.Int)

    object BoundDivisionBinaryOperator : BoundBinaryOperator(Operator.Slash, BoundType.Int)

    object BoundExponentiationBinaryOperator : BoundBinaryOperator(Operator.DoubleCircumflex, BoundType.Int)

    object BoundBitwiseAndBinaryOperator : BoundBinaryOperator(Operator.Ampersand, BoundType.Int)
    object BoundBitwiseXorBinaryOperator : BoundBinaryOperator(Operator.Circumflex, BoundType.Int)

    object BoundBitwiseOrBinaryOperator : BoundBinaryOperator(Operator.Pipe, BoundType.Int)
    object BoundRightShiftBinaryOperator : BoundBinaryOperator(Operator.DoubleGreaterThan, BoundType.Int)
    object BoundLeftShiftBinaryOperator : BoundBinaryOperator(Operator.DoubleLessThan, BoundType.Int)

    object BoundBitwiseLogicalAndBinaryOperator : BoundBinaryOperator(Operator.And, BoundType.Boolean)

    object BoundBitwiseLogicalOrBinaryOperator : BoundBinaryOperator(Operator.Or, BoundType.Boolean)

    object BoundBitwiseLogicalXorBinaryOperator : BoundBinaryOperator(Operator.Xor, BoundType.Boolean)
    object BoundEqualsBinaryOperator :
        BoundBinaryOperator(Operator.DoubleEquals, BoundType.Object, BoundType.Boolean)

    object BoundNotEqualsBinaryOperator :
        BoundBinaryOperator(Operator.NotEquals, BoundType.Object, BoundType.Boolean)

    object BoundLessThanBinaryOperator : BoundBinaryOperator(Operator.LessThan, BoundType.Int, BoundType.Boolean)

    object BoundGreaterThanBinaryOperator :
        BoundBinaryOperator(Operator.GreaterThan, BoundType.Int, BoundType.Boolean)

    object BoundLessThanOrEqualsBinaryOperator :
        BoundBinaryOperator(Operator.LessThanEquals, BoundType.Int, BoundType.Boolean)

    object BoundGreaterThanOrEqualsBinaryOperator :
        BoundBinaryOperator(Operator.GreaterThanEquals, BoundType.Int, BoundType.Boolean)


}