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
            is BoundArithmeticBinaryExpression -> {
                evaluateArithmeticBinaryExpression(expression)
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

    private fun evaluateBooleanBinaryExpression(expression: BoundBooleanBinaryExpression): Any {
        val left = evaluateExpression(expression.left)
        val right = evaluateExpression(expression.right)

        return when (expression.operatorKind) {
            BoundBooleanBinaryOperatorKind.And -> left as Boolean && right as Boolean
            BoundBooleanBinaryOperatorKind.Or -> left as Boolean || right as Boolean
            BoundBooleanBinaryOperatorKind.Xor -> left as Boolean xor right as Boolean
            BoundBooleanBinaryOperatorKind.Equals -> left == right
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

    private fun evaluateArithmeticBinaryExpression(expression: BoundArithmeticBinaryExpression): Any {
        val left = evaluateExpression(expression.left) as Int
        val right = evaluateExpression(expression.right) as Int
        return when (expression.operatorKind) {
            BoundArithmeticBinaryOperatorKind.Addition -> left + right
            BoundArithmeticBinaryOperatorKind.Subtraction -> left - right
            BoundArithmeticBinaryOperatorKind.Multiplication -> left * right
            BoundArithmeticBinaryOperatorKind.Division -> left / right
            BoundArithmeticBinaryOperatorKind.LogicalAnd -> left and right
            BoundArithmeticBinaryOperatorKind.LogicalOr -> left or right
        }
    }

}