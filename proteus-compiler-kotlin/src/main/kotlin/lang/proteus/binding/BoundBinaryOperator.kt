package lang.proteus.binding

import lang.proteus.syntax.lexer.Operator


internal sealed class BoundBinaryOperator(
    val operator: Operator, val leftType: ProteusType, val rightType: ProteusType, val resultType: ProteusType,
    val requiresSameTypes: Boolean = true
) {


    constructor(operator: Operator, type: ProteusType) : this(operator, type, type, type)

    constructor(operator: Operator, type: ProteusType, resultType: ProteusType) : this(
        operator,
        type,
        type,
        resultType
    )

    companion object {
        private val operators = BoundBinaryOperator::class.sealedSubclasses.map { it.objectInstance!! }

        fun bind(operator: Operator, leftType: ProteusType, rightType: ProteusType): BoundBinaryOperator? {
            return operators.firstOrNull {
                val isSuited = it.operator == operator && it.leftType.isAssignableTo(leftType) && it.rightType.isAssignableTo(
                    rightType
                )
                if(isSuited && it.requiresSameTypes) {
                    return@firstOrNull leftType == rightType
                }
                isSuited
            }
        }
    }

    object BoundAdditionBinaryOperator : BoundBinaryOperator(Operator.Plus, ProteusType.Int)

    object BoundSubtractionBinaryOperator : BoundBinaryOperator(Operator.Minus, ProteusType.Int)

    object BoundMultiplicationBinaryOperator : BoundBinaryOperator(Operator.Asterisk, ProteusType.Int)

    object BoundDivisionBinaryOperator : BoundBinaryOperator(Operator.Slash, ProteusType.Int)

    object BoundExponentiationBinaryOperator : BoundBinaryOperator(Operator.DoubleCircumflex, ProteusType.Int)

    object BoundBitwiseAndBinaryOperator : BoundBinaryOperator(Operator.Ampersand, ProteusType.Int)
    object BoundBitwiseXorBinaryOperator : BoundBinaryOperator(Operator.Circumflex, ProteusType.Int)

    object BoundBitwiseOrBinaryOperator : BoundBinaryOperator(Operator.Pipe, ProteusType.Int)
    object BoundRightShiftBinaryOperator : BoundBinaryOperator(Operator.DoubleGreaterThan, ProteusType.Int)
    object BoundLeftShiftBinaryOperator : BoundBinaryOperator(Operator.DoubleLessThan, ProteusType.Int)

    object BoundBitwiseLogicalAndBinaryOperator : BoundBinaryOperator(Operator.And, ProteusType.Boolean)

    object BoundBitwiseLogicalOrBinaryOperator : BoundBinaryOperator(Operator.Or, ProteusType.Boolean)

    object BoundBitwiseLogicalXorBinaryOperator : BoundBinaryOperator(Operator.Xor, ProteusType.Boolean)
    object BoundEqualsBinaryOperator :
        BoundBinaryOperator(Operator.DoubleEquals, ProteusType.Object, ProteusType.Boolean)

    object BoundNotEqualsBinaryOperator :
        BoundBinaryOperator(Operator.NotEquals, ProteusType.Object, ProteusType.Boolean)

    object BoundLessThanBinaryOperator : BoundBinaryOperator(Operator.LessThan, ProteusType.Int, ProteusType.Boolean)

    object BoundGreaterThanBinaryOperator :
        BoundBinaryOperator(Operator.GreaterThan, ProteusType.Int, ProteusType.Boolean)

    object BoundLessThanOrEqualsBinaryOperator :
        BoundBinaryOperator(Operator.LessThanEquals, ProteusType.Int, ProteusType.Boolean)

    object BoundGreaterThanOrEqualsBinaryOperator :
        BoundBinaryOperator(Operator.GreaterThanEquals, ProteusType.Int, ProteusType.Boolean)

    object BoundIsBinaryOperator : BoundBinaryOperator(
        Operator.Is,
        ProteusType.Object,
        ProteusType.Type,
        ProteusType.Boolean, requiresSameTypes = false)


}