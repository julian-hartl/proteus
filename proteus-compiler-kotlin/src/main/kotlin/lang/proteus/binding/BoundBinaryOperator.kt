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

internal enum class BoundBinaryOperatorKind {
    Addition,
    Subtraction,
    Multiplication,
    Division,
    Modulo,
    Exponentiation,
    LogicalAnd,
    LogicalOr,
    LogicalXor,
    BitwiseAnd,
    BitwiseOr,
    BitwiseXor,
    BitwiseShiftLeft,
    BitwiseShiftRight,
    Equality,
    Inequality,
    LessThan,
    LessThanOrEqual,
    GreaterThan,
    GreaterThanOrEqual,
    TypeEquality
}

internal data class BoundBinaryOperator(
    val kind: BoundBinaryOperatorKind,
    val operator: Operator, val leftType: TypeSymbol, val rightType: TypeSymbol, val resultType: TypeSymbol,
    val requiresSameTypes: Boolean = false,
) : BoundOperator() {


    constructor(kind: BoundBinaryOperatorKind, operator: Operator, type: TypeSymbol) : this(
        kind,
        operator,
        type,
        type,
        type,
        requiresSameTypes = true
    )

    constructor(kind: BoundBinaryOperatorKind, operator: Operator, type: TypeSymbol, resultType: TypeSymbol) : this(
        kind,
        operator,
        type,
        type,
        resultType,
        requiresSameTypes = true
    )

    companion object {
        private val operators: List<BoundBinaryOperator> = listOf(
            BoundBinaryOperator(
                BoundBinaryOperatorKind.TypeEquality,
                Operator.Is,
                TypeSymbol.Any,
                TypeSymbol.Type,
                TypeSymbol.Boolean,
            ),
            BoundBinaryOperator(BoundBinaryOperatorKind.Addition, Operator.Plus, TypeSymbol.Int),
            BoundBinaryOperator(BoundBinaryOperatorKind.Addition, Operator.Plus, TypeSymbol.String),
            BoundBinaryOperator(BoundBinaryOperatorKind.Subtraction, Operator.Minus, TypeSymbol.Int),
            BoundBinaryOperator(BoundBinaryOperatorKind.Multiplication, Operator.Asterisk, TypeSymbol.Int),
            BoundBinaryOperator(BoundBinaryOperatorKind.Division, Operator.Slash, TypeSymbol.Int),
            BoundBinaryOperator(BoundBinaryOperatorKind.Modulo, Operator.Percent, TypeSymbol.Int),
            BoundBinaryOperator(
                BoundBinaryOperatorKind.Equality,
                Operator.DoubleEquals,
                TypeSymbol.Any,
                TypeSymbol.Boolean
            ),
            BoundBinaryOperator(
                BoundBinaryOperatorKind.Inequality,
                Operator.NotEquals,
                TypeSymbol.Any,
                TypeSymbol.Boolean
            ),
            BoundBinaryOperator(
                BoundBinaryOperatorKind.LessThan,
                Operator.LessThan,
                TypeSymbol.Int,
                TypeSymbol.Boolean
            ),
            BoundBinaryOperator(
                BoundBinaryOperatorKind.LessThanOrEqual,
                Operator.LessThanEquals,
                TypeSymbol.Int,
                TypeSymbol.Boolean
            ),
            BoundBinaryOperator(
                BoundBinaryOperatorKind.GreaterThan,
                Operator.GreaterThan,
                TypeSymbol.Int,
                TypeSymbol.Boolean
            ),
            BoundBinaryOperator(
                BoundBinaryOperatorKind.GreaterThanOrEqual,
                Operator.GreaterThanEquals,
                TypeSymbol.Int,
                TypeSymbol.Boolean
            ),
            BoundBinaryOperator(BoundBinaryOperatorKind.LogicalAnd, Operator.And, TypeSymbol.Boolean),
            BoundBinaryOperator(BoundBinaryOperatorKind.LogicalOr, Operator.Or, TypeSymbol.Boolean),
            BoundBinaryOperator(BoundBinaryOperatorKind.LogicalXor, Operator.Xor, TypeSymbol.Boolean),
            BoundBinaryOperator(BoundBinaryOperatorKind.BitwiseShiftLeft, Operator.DoubleLessThan, TypeSymbol.Int),
            BoundBinaryOperator(BoundBinaryOperatorKind.BitwiseShiftRight, Operator.DoubleGreaterThan, TypeSymbol.Int),
            BoundBinaryOperator(BoundBinaryOperatorKind.Exponentiation, Operator.DoubleAsterisk, TypeSymbol.Int),
            BoundBinaryOperator(BoundBinaryOperatorKind.BitwiseAnd, Operator.Ampersand, TypeSymbol.Int),
            BoundBinaryOperator(BoundBinaryOperatorKind.BitwiseOr, Operator.Pipe, TypeSymbol.Int),
            BoundBinaryOperator(BoundBinaryOperatorKind.BitwiseXor, Operator.Circumflex, TypeSymbol.Int),

            )

        fun findByOperator(operator: Operator): List<BoundBinaryOperator> {
            return operators.filter { it.operator == operator }
        }

        fun bind(operator: Operator, leftType: TypeSymbol, rightType: TypeSymbol): BoundBinaryOperator? {
            return operators.firstOrNull {
                val isSuited =
                    it.operator == operator && leftType.isAssignableTo(it.leftType) && rightType.isAssignableTo(
                        it.rightType
                    )
                if (isSuited && it.requiresSameTypes) {
                    return@firstOrNull leftType == rightType
                }
                isSuited
            }
        }
    }


}