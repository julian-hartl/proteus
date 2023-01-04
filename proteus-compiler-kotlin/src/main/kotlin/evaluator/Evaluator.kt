package evaluator

import binding.*
import syntax.parser.*

class Evaluator(private val boundExpression: BoundExpression) {

    fun evaluate(): Any {
        return evaluateExpression(boundExpression)
    }

    private fun evaluateExpression(expression: BoundExpression): Any {
        // This suppression is needed in order to compile.
        @Suppress("REDUNDANT_ELSE_IN_WHEN")

        return when (expression) {
            is BoundLiteralExpression<*> -> expression.value
            is BoundNumberBinaryExpression -> {
                evaluateNumberBinaryExpression(expression)
            }

            is BoundGenericBinaryExpression -> {
                evaluateGenericBinaryExpression(expression)
            }

            is BoundBooleanBinaryExpression -> {
                evaluateBooleanBinaryExpression(expression)
            }

            is BoundUnaryExpression -> {
                evaluateUnaryExpression(expression)
            }

            else -> evaluateExpression(expression)
        }

    }

    private fun evaluateGenericBinaryExpression(expression: BoundGenericBinaryExpression): Any {
        val right = evaluateExpression(expression.right)
        val left = evaluateExpression(expression.left)
        return when (expression.operatorKind) {
            BoundGenericBinaryOperatorKind.Equals -> left == right
            BoundGenericBinaryOperatorKind.NotEquals -> left != right
        }

    }

    private fun evaluateBooleanBinaryExpression(expression: BoundBooleanBinaryExpression): Any {
        val left = evaluateExpression(expression.left)
        val right = evaluateExpression(expression.right)

        return when (expression.operatorKind) {
            BoundBooleanBinaryOperatorKind.And -> left as Boolean && right as Boolean
            BoundBooleanBinaryOperatorKind.Or -> left as Boolean || right as Boolean
            BoundBooleanBinaryOperatorKind.Xor -> left as Boolean xor right as Boolean
        }
    }


    private fun evaluateUnaryExpression(expression: BoundUnaryExpression): Any {
        val operand = evaluateExpression(expression.operand)
        return when (expression.operatorKind) {
            BoundUnaryOperatorKind.Identity -> operand as Int
            BoundUnaryOperatorKind.Negation -> -(operand as Int)
            BoundUnaryOperatorKind.Invert -> !(operand as Boolean)
        }
    }

    private fun evaluateNumberBinaryExpression(expression: BoundNumberBinaryExpression): Any {
        val left = evaluateExpression(expression.left) as Int
        val right = evaluateExpression(expression.right) as Int
        return when (expression.operatorKind) {
            BoundNumberBinaryOperatorKind.Addition -> left + right
            BoundNumberBinaryOperatorKind.Subtraction -> left - right
            BoundNumberBinaryOperatorKind.Multiplication -> left * right
            BoundNumberBinaryOperatorKind.Division -> left / right
            BoundNumberBinaryOperatorKind.LogicalAnd -> left and right
            BoundNumberBinaryOperatorKind.LogicalOr -> left or right
            BoundNumberBinaryOperatorKind.GreaterThan -> left > right
            BoundNumberBinaryOperatorKind.LessThan -> left < right
            BoundNumberBinaryOperatorKind.GreaterThanOrEqual -> left >= right
            BoundNumberBinaryOperatorKind.LessThanOrEqual -> left <= right
        }
    }

}