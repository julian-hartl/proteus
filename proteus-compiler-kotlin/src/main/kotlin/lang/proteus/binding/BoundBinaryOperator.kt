package lang.proteus.binding

import lang.proteus.syntax.lexer.token.Operator

internal sealed class BoundOperator {
    fun canBeApplied(vararg types: ProteusType): Boolean {
        return when (this) {
            is BoundBinaryOperator -> BoundBinaryOperator.bind(operator, types[0], types[1]) == this
            is BoundUnaryOperator -> BoundUnaryOperator.bind(operator, types[0]) == this
        }
    }
}

internal sealed class BoundBinaryOperator(
    val operator: Operator, val leftType: ProteusType, val rightType: ProteusType, val resultType: ProteusType,
    val requiresSameTypes: Boolean = true,
) : BoundOperator() {


    constructor(operator: Operator, type: ProteusType) : this(operator, type, type, type)

    constructor(operator: Operator, type: ProteusType, resultType: ProteusType) : this(
        operator,
        type,
        type,
        resultType
    )

    companion object {
        private val operators = lazy {
            BoundBinaryOperator::class.sealedSubclasses.filter { it.objectInstance != null }.map { it.objectInstance!! }
        }

        fun bind(operator: Operator, leftType: ProteusType, rightType: ProteusType): BoundBinaryOperator? {
            return operators.value.firstOrNull {
                val isSuited =
                    it.operator == operator && it.leftType.isAssignableTo(leftType) && it.rightType.isAssignableTo(
                        rightType
                    )
                if (isSuited && it.requiresSameTypes) {
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

    object BoundExponentiationBinaryOperator : BoundBinaryOperator(Operator.DoubleAsterisk, ProteusType.Int)

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
        ProteusType.Boolean, requiresSameTypes = false
    )

    object BoundModuloBinaryOperator : BoundBinaryOperator(
        Operator.Percent,
        ProteusType.Int,
    )


}