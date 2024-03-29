package lang.proteus.evaluator

import lang.proteus.binding.*
import lang.proteus.symbols.*
import java.util.*

internal class Evaluator(
    private val functionBodies: Map<FunctionSymbol, BoundBlockStatement>,
    private val mainFunction: FunctionSymbol,
    private val globalVariableInitializers: Map<GlobalVariableSymbol, BoundExpression>,
    private val structMembers: Map<StructSymbol, Set<StructMemberSymbol>>,
) {
    private val locals: Stack<MutableMap<String, Any>> = Stack()
    private val globals: MutableMap<String, Any> = mutableMapOf()
    private var lastValue: Any? = null

    fun evaluate(): Any? {
        evaluateGlobalVariableInitializers()
        evaluateCallExpression(BoundCallExpression(mainFunction, emptyList()))
        return lastValue
    }

    private fun evaluateGlobalVariableInitializers() {
        for ((symbol, initializer) in globalVariableInitializers) {
            globals[symbol.qualifiedName] = evaluateExpression(initializer)!!
        }
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
                evaluateFlattened(statement, index, labelToIndex, body.statements.size)
            } catch (e: Exception) {
                e.printStackTrace()
                index + 1
            }
            index = newIndex
        }
        return lastValue
    }

    private fun evaluateFlattened(
        statement: BoundStatement,
        currentIndex: Int,
        labels: Map<BoundLabel, Int>,
        totalStatements: Int,
    ): Int {
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

            is BoundNopStatement -> {
                currentIndex + 1
            }

            is BoundReturnStatement -> {

                if (statement.expression != null) lastValue = evaluateExpression(statement.expression)
                totalStatements
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
            locals.peek()[statement.variable.qualifiedName] = value!!
        } else {
            globals[statement.variable.qualifiedName] = value!!
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
            is BoundStructInitializationExpression -> evaluateStructInitializationExpression(expression)
            is BoundMemberAccessExpression -> evaluateMemberAccessExpression(expression)
            is BoundTypeExpression -> expression.symbol
            is BoundReferenceExpression -> evaluateReferenceExpression(expression)
            else -> throwUnsupportedOperation(expression::class.simpleName!!)
        }

    }

    private fun evaluateReferenceExpression(expression: BoundReferenceExpression): Any? {
        return evaluateExpression(expression.expression)
    }

    private fun evaluateMemberAccessExpression(expression: BoundMemberAccessExpression): Any? {
        val value = evaluateExpression(expression.expression)!!
        if (isAssignmentAccess) {
            return value
        }
        return (value as Map<*, *>)[expression.memberName]
    }

    private fun evaluateStructInitializationExpression(expression: BoundStructInitializationExpression): Any? {
        val struct = expression.struct
        val values = expression.members.map { evaluateExpression(it.expression)!! }
        val members = structMembers[struct]!!
        val stackFrame = mutableMapOf<String, Any>()
        for ((index, field) in members.withIndex()) {
            stackFrame[field.name] = values[index]
        }
        return stackFrame
    }

    private fun evaluateConversionExpression(expression: BoundConversionExpression): Any {
        val value = evaluateExpression(expression.expression)
        return TypeConverter.convert(value!!, expression.type)
    }

    private fun evaluateCallExpression(callExpression: BoundCallExpression): Any? {
        val function = callExpression.function
        val arguments = callExpression.arguments.map { evaluateExpression(it)!! }
        if (function.declaration.isExternal) {
            val externalFunction = ProteusExternalFunction.lookup(function.declaration)!!
            return externalFunction.call(arguments)
        }
        val stackFrame = mutableMapOf<String, Any>()
        for ((index, parameter) in function.parameters.withIndex()) {
            stackFrame[parameter.qualifiedName] = arguments[index]
        }
        locals.push(stackFrame)
        val statement = functionBodies[function]
        val value = evaluateStatement(statement ?: throw IllegalStateException("Function body not found for $function"))
        locals.pop()
        return value
    }

    private fun evaluateVariableExpression(expression: BoundVariableExpression): Any {
        if (expression.variable.isLocal) {
            if (locals.isEmpty()) throw IllegalStateException("No locals found for ${expression.variable.qualifiedName}")
            return locals.peek()[expression.variable.qualifiedName]
                ?: throw IllegalStateException("No locals found for ${expression.variable.simpleName}")
        }
        return globals[expression.variable.qualifiedName]
            ?: throw IllegalStateException("No globals found for ${expression.variable.simpleName}")
    }

    private var isAssignmentAccess = false

    private fun evaluateAssignmentExpression(expression: BoundAssignmentExpression): Any {
        var assignee: BoundAssignee<*>? = expression.assignee
        var currentValue: Any? = null
        val expressionValue = evaluateExpression(expression.expression)
        while (assignee != null) {
            when (assignee) {
                is BoundAssignee.BoundDereferenceAssignee -> {
                    isAssignmentAccess = true
                    assignee = assignee.referencing
                }

                is BoundAssignee.BoundMemberAssignee -> {
                    isAssignmentAccess = true
                    val value = evaluateExpression(assignee.expression)!!
                    isAssignmentAccess = false
                    (value as MutableMap<String, Any>)[assignee.expression.memberName] = expressionValue!!
                    currentValue = value
                    assignee = null
                }

                is BoundAssignee.BoundVariableAssignee -> {
                    val variable = assignee.variable
                    currentValue = if (variable.isLocal) {
                        locals.peek()[variable.qualifiedName]
                            ?: throw IllegalStateException("No locals found for ${variable.simpleName}")
                    } else {
                        globals[variable.qualifiedName]
                            ?: throw IllegalStateException("No globals found for ${variable.simpleName}")
                    }
                    if (variable.isGlobal) {
                        globals[variable.qualifiedName] = expressionValue!!
                    } else {
                        locals.peek()[variable.qualifiedName] = expressionValue!!
                    }
                    assignee = null
                }
            }
        }


        return if (expression.returnAssignment) expressionValue!! else currentValue!!
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