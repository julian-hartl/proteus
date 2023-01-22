package lang.proteus.evaluator

import lang.proteus.binding.BoundBinaryOperatorKind
import lang.proteus.symbols.TypeSymbol
import kotlin.math.pow

internal object BinaryExpressionEvaluator {
    fun evaluate(kind: BoundBinaryOperatorKind, resultType: TypeSymbol, left: Any, right: Any): Any {
        return when (kind) {
            BoundBinaryOperatorKind.Addition -> {
                if (resultType == TypeSymbol.Int) {
                    (left as Int) + (right as Int)
                } else {
                    (left as String) + (right as String)
                }
            }

            BoundBinaryOperatorKind.Subtraction -> left as Int - right as Int
            BoundBinaryOperatorKind.Division -> left as Int / right as Int
            BoundBinaryOperatorKind.Multiplication -> left as Int * right as Int
            BoundBinaryOperatorKind.Exponentiation -> (left as Int).toDouble().pow(right as Int).toInt()
            BoundBinaryOperatorKind.Modulo -> left as Int % right as Int
            BoundBinaryOperatorKind.BitwiseAnd -> left as Int and right as Int
            BoundBinaryOperatorKind.BitwiseXor -> left as Int xor right as Int
            BoundBinaryOperatorKind.BitwiseOr -> left as Int or right as Int
            BoundBinaryOperatorKind.LogicalAnd -> left as Boolean and right as Boolean
            BoundBinaryOperatorKind.LogicalOr -> left as Boolean or right as Boolean
            BoundBinaryOperatorKind.LogicalXor -> left as Boolean xor right as Boolean
            BoundBinaryOperatorKind.Equality -> left == right
            BoundBinaryOperatorKind.Inequality -> left != right
            BoundBinaryOperatorKind.GreaterThan -> left as Int > right as Int
            BoundBinaryOperatorKind.GreaterThanOrEqual -> left as Int >= right as Int
            BoundBinaryOperatorKind.LessThan -> (left as Int) < (right as Int)
            BoundBinaryOperatorKind.LessThanOrEqual -> left as Int <= right as Int
            BoundBinaryOperatorKind.BitwiseShiftLeft -> left as Int shl right as Int
            BoundBinaryOperatorKind.BitwiseShiftRight -> left as Int shr right as Int
            BoundBinaryOperatorKind.TypeEquality -> (right as TypeSymbol).isAssignableTo(
                TypeSymbol.fromValueOrAny(
                    left
                )
            )

        }
    }
}