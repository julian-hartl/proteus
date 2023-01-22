package lang.proteus.evaluator

import lang.proteus.binding.*
import lang.proteus.symbols.FunctionSymbol
import lang.proteus.symbols.ProteusExternalFunction
import lang.proteus.symbols.TypeSymbol
import java.util.*

internal class Evaluator(
    private val root: BoundBlockStatement,
    private val globals: MutableMap<String, Any>,
    private val functionBodies: Map<FunctionSymbol, BoundBlockStatement>,
    private val locals: Stack<MutableMap<String, Any>> = Stack(),
) {

    private var lastValue: Any? = null

    fun evaluate(): Any? {
        return evaluateStatement(root)
    }

    private fun evaluateStatement(body: BoundBlockStatement): Any? {
        val labelToIndex = mutableMapOf<BoundLabel, Int>()
        for ((index, statement) in body.statements.withIndex()) {
            if (statement is BoundLabelStatement) {
                labelToIndex[statement.label] = index + 1
            }
        }

        var index = 0
        while (index < body.statements.size) {
            val statement = body.statements[index]
            val newIndex = try {
                evaluateFlattened(statement, index, labelToIndex)
            } catch (e: Exception) {
                e.printStackTrace()
                index + 1
            }
            index = newIndex
        }
        return lastValue
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
        if (statement.variable.isLocal) {
            locals.peek()[statement.variable.name] = value!!
        } else {
            globals[statement.variable.name] = value!!
        }
        lastValue = value
    }

    private fun evaluateExpressionStatement(statement: BoundExpressionStatement) {
        val value = evaluateExpression(statement.expression)
        lastValue = value
    }

    private fun evaluateExpression(expression: BoundExpression): Any? {
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
            is BoundCallExpression -> evaluateCallExpression(expression)
            is BoundConversionExpression -> evaluateConversionExpression(expression)
            else -> throwUnsupportedOperation(expression::class.simpleName!!)
        }

    }

    private fun evaluateConversionExpression(expression: BoundConversionExpression): Any? {
        val value = evaluateExpression(expression.expression)
        return convert(value!!, expression.type)
    }

    private fun convert(value: Any, type: TypeSymbol): Any {
        return when (type) {
            TypeSymbol.Int -> Integer.parseInt(value.toString())
            TypeSymbol.Boolean -> parseBoolean(value.toString())
            TypeSymbol.String -> value.toString()
            else -> throwUnsupportedOperation(type.name)
        }
    }

    private fun parseBoolean(toString: String): Any {
        return when (toString) {
            "true" -> true
            "false" -> false
            else -> throw IllegalStateException("Cannot parse $toString to boolean")
        }
    }

    private fun evaluateCallExpression(callExpression: BoundCallExpression): Any? {
        val function = callExpression.functionSymbol
        val arguments = callExpression.arguments.map { evaluateExpression(it)!! }
        if (callExpression.isExternal) {
            val externalFunction = ProteusExternalFunction.lookup(function.name)!!
            return externalFunction.call(arguments)
        }
        val stackFrame = mutableMapOf<String, Any>()
        for ((index, parameter) in function.parameters.withIndex()) {
            stackFrame[parameter.name] = arguments[index]
        }
        locals.push(stackFrame)
        val statement = functionBodies[function]
        val value = evaluateStatement(statement ?: throw IllegalStateException("Function body not found for $function"))
        locals.pop()
        return value
    }

    private fun evaluateVariableExpression(expression: BoundVariableExpression): Any {
        if (expression.variable.isLocal) {
            if (locals.isEmpty()) throw IllegalStateException("No locals found for ${expression.variable.name}")
            return locals.peek()[expression.variable.name]!!
        }
        return globals[expression.variable.name]!!
    }

    private fun evaluateAssignmentExpression(expression: BoundAssignmentExpression): Any {
        val expressionValue = evaluateExpression(expression.expression)
        if (expression.variable.isGlobal) {
            globals[expression.variable.name] = expressionValue!!
        } else {
            locals.peek()[expression.variable.name] = expressionValue!!
        }

        return expressionValue
    }

    private fun evaluateBinaryExpression(expression: BoundBinaryExpression): Any {
        val left = evaluateExpression(expression.left)
        val right = evaluateExpression(expression.right)


        return BinaryExpressionEvaluator.evaluate(expression.operator.kind, expression.type, left!!, right!!)

    }


    private fun evaluateUnaryExpression(expression: BoundUnaryExpression): Any {
        val operand = evaluateExpression(expression.operand)
        return UnaryExpressionEvaluator.evaluate(expression.operator, operand!!)
    }


}