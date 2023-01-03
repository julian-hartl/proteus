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
            is BoundBinaryExpression -> {
                evaluateBinaryExpression(expression)
            }

            is BoundUnaryExpression -> {
                evaluateUnaryExpression(expression)
            }

            else -> evaluateExpression(expression)
        }

    }

    private fun evaluateUnaryExpression(expression: BoundUnaryExpression): Any {
        val operand = evaluateExpression(expression.operand)
        return when (expression.operatorKind) {
            BoundUnaryOperatorKind.Identity -> operand as Int
            BoundUnaryOperatorKind.Negation -> -(operand as Int)
        }
    }

    private fun evaluateBinaryExpression(expression: BoundBinaryExpression): Any {
        val left = evaluateExpression(expression.left) as Int
        val right = evaluateExpression(expression.right) as Int
        return when (expression.operatorKind) {
            BoundBinaryOperatorKind.Addition -> left + right
            BoundBinaryOperatorKind.Subtraction -> left - right
            BoundBinaryOperatorKind.Multiplication -> left * right
            BoundBinaryOperatorKind.Division -> left / right
            BoundBinaryOperatorKind.LogicalAnd -> left and right
            BoundBinaryOperatorKind.LogicalOr -> left or right
        }
    }

}