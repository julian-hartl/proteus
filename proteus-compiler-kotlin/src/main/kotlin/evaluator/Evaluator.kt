package evaluator

import binding.*
import syntax.parser.*
import kotlin.math.pow

class Evaluator(private val boundExpression: BoundExpression) {

    fun evaluate(): Any {
        return evaluateExpression(boundExpression)
    }

    private fun evaluateExpression(expression: BoundExpression): Any {
        // This suppression is needed in order to compile.
        @Suppress("REDUNDANT_ELSE_IN_WHEN")

        return when (expression) {
            is BoundLiteralExpression<*> -> expression.value
            is BoundBinaryExpression -> evaluateBinaryExpression(expression)

            is BoundUnaryExpression -> {
                evaluateUnaryExpression(expression)
            }

            else -> evaluateExpression(expression)
        }

    }

    private fun evaluateBinaryExpression(expression: BoundBinaryExpression): Any {
        val left = evaluateExpression(expression.left)
        val right = evaluateExpression(expression.right)

        return when (expression.operator) {
            BoundBinaryOperator.BoundAdditionBinaryOperator -> left as Int + right as Int
            BoundBinaryOperator.BoundSubtractionBinaryOperator -> left as Int - right as Int
            BoundBinaryOperator.BoundDivisionBinaryOperator -> left as Int / right as Int
            BoundBinaryOperator.BoundMultiplicationBinaryOperator -> left as Int * right as Int
            BoundBinaryOperator.BoundExponentiationBinaryOperator -> (left as Int).toDouble().pow(right as Int).toInt()
            BoundBinaryOperator.BoundBitwiseAndBinaryOperator -> left as Int and right as Int
            BoundBinaryOperator.BoundBitwiseXorBinaryOperator -> left as Int xor  right as Int
            BoundBinaryOperator.BoundBitwiseOrBinaryOperator -> left as Int or right as Int
            BoundBinaryOperator.BoundBitwiseLogicalAndBinaryOperator -> left as Boolean and right as Boolean
            BoundBinaryOperator.BoundBitwiseLogicalOrBinaryOperator -> left as Boolean or right as Boolean
            BoundBinaryOperator.BoundBitwiseLogicalXorBinaryOperator -> left as Boolean xor right as Boolean
            BoundBinaryOperator.BoundEqualsBinaryOperator -> left == right
            BoundBinaryOperator.BoundNotEqualsBinaryOperator -> left != right
            BoundBinaryOperator.BoundGreaterThanBinaryOperator -> left as Int > right as Int
            BoundBinaryOperator.BoundGreaterThanOrEqualsBinaryOperator -> left as Int >= right as Int
            BoundBinaryOperator.BoundLessThanBinaryOperator -> (left as Int) < (right as Int)
            BoundBinaryOperator.BoundLessThanOrEqualsBinaryOperator -> left as Int <= right as Int
            BoundBinaryOperator.BoundLeftShiftBinaryOperator -> left as Int shl right as Int
            BoundBinaryOperator.BoundRightShiftBinaryOperator -> left as Int shr right as Int
        }

    }


    private fun evaluateUnaryExpression(expression: BoundUnaryExpression): Any {
        val operand = evaluateExpression(expression.operand)
        return when (expression.operator) {
            BoundUnaryOperator.BoundUnaryIdentityOperator -> operand as Int
            BoundUnaryOperator.BoundUnaryNegationOperator -> -(operand as Int)
            BoundUnaryOperator.BoundUnaryNotOperator -> !(operand as Boolean)

            else -> {
                throw Exception("Unexpected unary operator ${expression.operator}")
            }
        }
    }


}