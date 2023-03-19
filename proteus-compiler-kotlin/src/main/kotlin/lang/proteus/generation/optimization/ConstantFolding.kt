package lang.proteus.generation.optimization

import lang.proteus.binding.*
import lang.proteus.evaluator.BinaryExpressionEvaluator
import lang.proteus.evaluator.UnaryExpressionEvaluator
import lang.proteus.symbols.VariableSymbol

internal fun isConstStatement(statement: BoundStatement): Boolean {
    return when (statement) {
        is BoundBlockStatement -> statement.statements.all { isConstStatement(it) }
        is BoundVariableDeclaration -> statement.variable.isConst
        is BoundExpressionStatement -> isConstExpression(statement.expression)
        is BoundGotoStatement -> true
        is BoundConditionalGotoStatement -> isConstExpression(statement.condition)
        is BoundLabelStatement -> true
        is BoundReturnStatement -> if (statement.expression == null) true else isConstExpression(statement.expression)
        is BoundNopStatement -> true
        is BoundBreakStatement -> true
        is BoundContinueStatement -> true
        else -> {
            throw Exception("Unexpected statement: $statement")
        }
    }
}

internal fun getConstantValue(expression: BoundExpression): Any? {

    return when (expression) {
        is BoundLiteralExpression<*> -> expression.value
        is BoundVariableExpression -> {
            val constantValue = expression.variable.constantValue
            if (constantValue == null) {
                null
            } else {
                getConstantValue(constantValue)
            }
        }

        is BoundUnaryExpression -> {
            val operand = getConstantValue(expression.operand)
            if (operand == null) {
                null
            } else {
                UnaryExpressionEvaluator.evaluate(expression.operator, operand)
            }
        }

        is BoundBinaryExpression -> {
            val left = getConstantValue(expression.left)
            val right = getConstantValue(expression.right)
            if (left == null || right == null) {
                null
            } else {
                BinaryExpressionEvaluator.evaluate(expression.operator.kind, expression.type, left, right)
            }
        }

        is BoundCallExpression -> null
        is BoundConversionExpression -> getConstantValue(expression.expression)
        is BoundAssignmentExpression -> null
        BoundErrorExpression -> null
        is BoundMemberAccessExpression -> null
        is BoundStructInitializationExpression -> null
        is BoundTypeExpression -> null
    }
}

internal fun isConstExpression(expression: BoundExpression): Boolean {
    return getConstantValue(expression) != null
}

internal class ConstantFolding :
    BoundTreeRewriter(), Optimizer {


    private val assignments: MutableMap<VariableSymbol, List<BoundExpression>> = mutableMapOf()

    private val declarations: MutableMap<VariableSymbol, BoundVariableDeclaration> = mutableMapOf()


    override fun optimizeExpression(expression: BoundExpression): BoundExpression {
        return rewriteExpression(expression)
    }

    override fun optimize(statement: BoundBlockStatement): BoundBlockStatement {
        var lastStatement = rewriteBlockStatement(statement)

        var lastSize = -1
        while (true) {
            for ((variable, assignedExpressions) in assignments) {
                if (assignedExpressions.size == 1 && declarations[variable] != null) {
                    val assignedExpression = assignedExpressions.first()
                    if (assignedExpression is BoundLiteralExpression<*>) {
                        variable.constantValue = assignedExpression
                    }
                }
            }
            val size = assignments.size
            if (size == lastSize) {
                break
            }
            lastSize = size
            assignments.clear()
            lastStatement = rewriteBlockStatement(lastStatement)
        }
        return lastStatement
    }


    private fun isFunctionConst(function: BoundBlockStatement): Boolean {
        return function.statements.all { isConstStatement(it) }
    }


    override fun rewriteConditionalGotoStatement(statement: BoundConditionalGotoStatement): BoundStatement {
        val condition = rewriteExpression(statement.condition)
        if (condition is BoundLiteralExpression<*>) {
            val value = condition.value as Boolean
            if (statement.jumpIfFalse == value) {
                return BoundNopStatement()
            }
            return BoundGotoStatement(statement.label)
        }
        return statement
    }

    override fun rewriteUnaryExpression(node: BoundUnaryExpression): BoundExpression {
        val operand = rewriteExpression(node.operand)
        if (operand is BoundLiteralExpression<*>) {
            val value = UnaryExpressionEvaluator.evaluate(node.operator, operand.value)
            return BoundLiteralExpression(value)
        }
        if (operand == node.operand) {
            return node
        }
        return BoundUnaryExpression(operand, node.operator)
    }

    override fun rewriteVariableDeclaration(node: BoundVariableDeclaration): BoundStatement {
        val boundStatement = kotlin.run {
            if (node.variable.isConst) {
                return@run BoundNopStatement()
            }
            val initializer = rewriteExpression(node.initializer)
            assignments[node.variable] = listOf(initializer)
            if (initializer == node.initializer) {
                return@run node
            }
            return@run BoundVariableDeclaration(node.variable, initializer)
        }
        declarations[node.variable] = node
        return boundStatement
    }

    override fun rewriteCallExpression(expression: BoundCallExpression): BoundExpression {
        val arguments = expression.arguments.map { rewriteExpression(it) }
        if (arguments == expression.arguments) {
            return expression
        }
        return BoundCallExpression(expression.function, arguments)
    }

    override fun rewriteAssignmentExpression(node: BoundAssignmentExpression): BoundExpression {
        val expression = rewriteExpression(node.expression)
        if (node.assignee.expression is BoundVariableExpression) {
            val variable = node.assignee.expression.variable
            assignments[variable] = assignments[variable].orEmpty() + expression
        }
        if (expression == node.expression) {
            return node
        }
        return BoundAssignmentExpression(node.assignee, expression, node.assignmentOperator, node.returnAssignment)
    }

    override fun rewriteBinaryExpression(node: BoundBinaryExpression): BoundExpression {
        val left = rewriteExpression(node.left)
        val right = rewriteExpression(node.right)
        if (left is BoundLiteralExpression<*> && right is BoundLiteralExpression<*>) {
            val value = BinaryExpressionEvaluator.evaluate(node.operator.kind, node.type, left.value, right.value)
            return BoundLiteralExpression(value)
        }
        if (left == node.left && right == node.right) {
            return node
        }
        return BoundBinaryExpression(left, right, node.operator)
    }

    override fun rewriteBlockStatement(node: BoundBlockStatement): BoundBlockStatement {
        val statements = node.statements.map { rewriteStatement(it) }.filter {
            it !is BoundNopStatement
        }
        if (statements == node.statements) {
            return node
        }
        return BoundBlockStatement(statements)
    }

    override fun rewriteConversionExpression(expression: BoundConversionExpression): BoundExpression {
        val rewrittenExpression = rewriteExpression(expression.expression)
        if (rewrittenExpression is BoundLiteralExpression<*>) {
            val value = TypeConverter.convert(rewrittenExpression.value, expression.type)
            return BoundLiteralExpression(value)
        }
        if (rewrittenExpression == expression) {
            return expression
        }
        return BoundConversionExpression(expression.type, expression.expression, expression.conversion)
    }

    override fun rewriteExpressionStatement(statement: BoundExpressionStatement): BoundStatement {
        val rewrittenExpression = rewriteExpression(statement.expression)
        if (rewrittenExpression == statement.expression) {
            return statement
        }
        return BoundExpressionStatement(rewrittenExpression)
    }

    override fun rewriteVariableExpression(expression: BoundVariableExpression): BoundExpression {
        if (expression.variable.isConst) {
            return expression.variable.constantValue!!
        }
        return expression
    }

}