package lang.proteus.binding

import lang.proteus.symbols.TypeSymbol

import lang.proteus.syntax.lexer.token.Operator

internal sealed class BoundOperator {
    fun canBeApplied(vararg types: TypeSymbol): Boolean {
        return when (this) {
            is BoundBinaryOperator -> BoundBinaryOperator.bind(operator, types[0], types[1]) == this
            is BoundUnaryOperator -> BoundUnaryOperator.bind(operator, types[0]) == this
        }
    }
}

internal sealed class BoundBinaryOperator(
    val operator: Operator, val leftType: TypeSymbol, val rightType: TypeSymbol, val resultType: TypeSymbol,
    val requiresSameTypes: Boolean = true,
) : BoundOperator() {


    constructor(operator: Operator, type: TypeSymbol) : this(operator, type, type, type)

    constructor(operator: Operator, type: TypeSymbol, resultType: TypeSymbol) : this(
        operator,
        type,
        type,
        resultType
    )

    companion object {
        private val operators = lazy {
            BoundBinaryOperator::class.sealedSubclasses.filter { it.objectInstance != null }.map { it.objectInstance!! }
        }

        fun bind(operator: Operator, leftType: TypeSymbol, rightType: TypeSymbol): BoundBinaryOperator? {
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

    object BoundAdditionBinaryOperator : BoundBinaryOperator(Operator.Plus, TypeSymbol.Int)

    object BoundSubtractionBinaryOperator : BoundBinaryOperator(Operator.Minus, TypeSymbol.Int)

    object BoundMultiplicationBinaryOperator : BoundBinaryOperator(Operator.Asterisk, TypeSymbol.Int)

    object BoundDivisionBinaryOperator : BoundBinaryOperator(Operator.Slash, TypeSymbol.Int)

    object BoundExponentiationBinaryOperator : BoundBinaryOperator(Operator.DoubleAsterisk, TypeSymbol.Int)

    object BoundBitwiseAndBinaryOperator : BoundBinaryOperator(Operator.Ampersand, TypeSymbol.Int)
    object BoundBitwiseXorBinaryOperator : BoundBinaryOperator(Operator.Circumflex, TypeSymbol.Int)

    object BoundBitwiseOrBinaryOperator : BoundBinaryOperator(Operator.Pipe, TypeSymbol.Int)
    object BoundRightShiftBinaryOperator : BoundBinaryOperator(Operator.DoubleGreaterThan, TypeSymbol.Int)
    object BoundLeftShiftBinaryOperator : BoundBinaryOperator(Operator.DoubleLessThan, TypeSymbol.Int)

    object BoundBitwiseLogicalAndBinaryOperator : BoundBinaryOperator(Operator.And, TypeSymbol.Boolean)

    object BoundBitwiseLogicalOrBinaryOperator : BoundBinaryOperator(Operator.Or, TypeSymbol.Boolean)

    object BoundBitwiseLogicalXorBinaryOperator : BoundBinaryOperator(Operator.Xor, TypeSymbol.Boolean)
    object BoundEqualsBinaryOperator :
        BoundBinaryOperator(Operator.DoubleEquals, TypeSymbol.Any, TypeSymbol.Boolean)

    object BoundNotEqualsBinaryOperator :
        BoundBinaryOperator(Operator.NotEquals, TypeSymbol.Any, TypeSymbol.Boolean)

    object BoundLessThanBinaryOperator : BoundBinaryOperator(Operator.LessThan, TypeSymbol.Int, TypeSymbol.Boolean)

    object BoundGreaterThanBinaryOperator :
        BoundBinaryOperator(Operator.GreaterThan, TypeSymbol.Int, TypeSymbol.Boolean)

    object BoundLessThanOrEqualsBinaryOperator :
        BoundBinaryOperator(Operator.LessThanEquals, TypeSymbol.Int, TypeSymbol.Boolean)

    object BoundGreaterThanOrEqualsBinaryOperator :
        BoundBinaryOperator(Operator.GreaterThanEquals, TypeSymbol.Int, TypeSymbol.Boolean)

    object BoundIsBinaryOperator : BoundBinaryOperator(
        Operator.Is,
        TypeSymbol.Any,
        TypeSymbol.Type,
        TypeSymbol.Boolean, requiresSameTypes = false
    )

    object BoundModuloBinaryOperator : BoundBinaryOperator(
        Operator.Percent,
        TypeSymbol.Int,
    )


}