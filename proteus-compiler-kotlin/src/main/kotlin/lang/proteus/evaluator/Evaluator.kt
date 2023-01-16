package lang.proteus.evaluator

import lang.proteus.binding.*
import lang.proteus.syntax.lexer.token.Operator
import kotlin.math.pow

internal class Evaluator(private val root: BoundBlockStatement, private val variables: MutableMap<String, Any>) {

    private var lastValue: Any? = null

    fun evaluate(): Any {
        val labelToIndex = mutableMapOf<BoundLabel, Int>()
        for ((index, statement) in root.statements.withIndex()) {
            if (statement is BoundLabelStatement) {
                labelToIndex[statement.label] = index + 1
            }
        }

        var index = 0
        while (index < root.statements.size) {
            val statement = root.statements[index]
            val newIndex = evaluateFlattened(statement, index, labelToIndex)
            index = newIndex
        }
        return lastValue ?: "null"
    }

    private fun evaluateFlattened(statement: BoundStatement, currentIndex: Int, labels: Map<BoundLabel, Int>): Int {
        return when (statement) {
            is BoundExpressionStatement -> {
                evaluateExpressionStatement(statement)
                currentIndex + 1
            }

            is BoundVariableDeclaration -> {
                evaluateVariableDeclaration(statement)
                currentIndex + 1
            }

            is BoundConditionalGotoStatement -> {
                val conditionValue = evaluateExpression(statement.condition) as Boolean
                if (!conditionValue && statement.jumpIfFalse || conditionValue && !statement.jumpIfFalse) {
                    return labels[statement.label]!!
                }
                currentIndex + 1
            }

            is BoundGotoStatement -> {
                labels[statement.label]!!
            }

            is BoundLabelStatement -> {
                currentIndex + 1
            }

            else -> {
                throwUnsupportedOperation(statement::class.simpleName!!)
            }
        }
    }


    private fun throwUnsupportedOperation(operation: String): Nothing {
        throw UnsupportedOperationException("Operation $operation is not supported. Please implement it using one of the supported operations.")
    }

    private fun evaluateVariableDeclaration(statement: BoundVariableDeclaration) {
        val value = evaluateExpression(statement.initializer)
        variables[statement.variable.name] = value
        lastValue = value
    }

    private fun evaluateExpressionStatement(statement: BoundExpressionStatement) {
        val value = evaluateExpression(statement.expression)
        lastValue = value
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

            is BoundVariableExpression -> {
                evaluateVariableExpression(expression)
            }

            is BoundAssignmentExpression -> evaluateAssignmentExpression(expression)
        }

    }

    private fun evaluateVariableExpression(expression: BoundVariableExpression): Any {
        return variables[expression.symbol.name]!!
    }

    private fun evaluateAssignmentExpression(expression: BoundAssignmentExpression): Any {
        val expressionValue = evaluateExpression(expression.expression)
        val value = when (expression.assignmentOperator) {
            Operator.Equals -> expressionValue
            Operator.MinusEquals -> (variables[expression.variableSymbol.name] as Int) - (expressionValue as Int)
            Operator.PlusEquals -> (variables[expression.variableSymbol.name] as Int) + (expressionValue as Int)
        }
        variables[expression.variableSymbol.name] = value
        return value
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
            BoundBinaryOperator.BoundModuloBinaryOperator -> left as Int % right as Int
            BoundBinaryOperator.BoundBitwiseAndBinaryOperator -> left as Int and right as Int
            BoundBinaryOperator.BoundBitwiseXorBinaryOperator -> left as Int xor right as Int
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
            BoundBinaryOperator.BoundIsBinaryOperator -> (right as ProteusType).isAssignableTo(
                ProteusType.fromValueOrObject(
                    left
                )
            )

        }

    }


    private fun evaluateUnaryExpression(expression: BoundUnaryExpression): Any {
        val operand = evaluateExpression(expression.operand)
        return when (expression.operator) {
            BoundUnaryOperator.BoundUnaryIdentityOperator -> operand as Int
            BoundUnaryOperator.BoundUnaryNegationOperator -> -(operand as Int)
            BoundUnaryOperator.BoundUnaryNotOperator -> !(operand as Boolean)
            BoundUnaryOperator.BoundUnaryTypeOfOperator -> ProteusType.fromValueOrObject(operand)
        }
    }


}