package lang.proteus.generation

import lang.proteus.binding.*
import lang.proteus.evaluator.BinaryExpressionEvaluator
import lang.proteus.evaluator.UnaryExpressionEvaluator
import lang.proteus.symbols.VariableSymbol


internal class ConstOptimizer private constructor(

    ) :
    BoundTreeRewriter() {
    companion object {
        fun optimize(
            statement: BoundBlockStatement,
        ): BoundBlockStatement {
            val optimizer = ConstOptimizer()
            return optimizer.optimize(statement)
        }

        fun optimize(statement: BoundExpression): BoundExpression {
            val optimizer = ConstOptimizer()
            return optimizer.optimizeExpression(statement)
        }
    }

    private val assignments: MutableMap<VariableSymbol, List<BoundExpression>> = mutableMapOf()

    private val declarations: MutableMap<VariableSymbol, BoundVariableDeclaration> = mutableMapOf()


    private fun optimizeExpression(expression: BoundExpression): BoundExpression {
        return rewriteExpression(expression)
    }

    private fun optimize(statement: BoundBlockStatement): BoundBlockStatement {
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

    private fun isConstExpression(expression: BoundExpression): Boolean {
        return when (expression) {
            is BoundLiteralExpression<*> -> true
            is BoundVariableExpression -> expression.variable.isConst
            is BoundUnaryExpression -> isConstExpression(expression.operand)
            is BoundBinaryExpression -> isConstExpression(expression.left) && isConstExpression(expression.right)
            is BoundCallExpression -> expression.arguments.all { isConstExpression(it) }
            is BoundConversionExpression -> isConstExpression(expression.expression)
            is BoundAssignmentExpression -> isConstExpression(expression.expression)
            BoundErrorExpression -> true
            is BoundMemberAccessExpression -> isConstExpression(expression.expression)
            is BoundStructInitializationExpression -> expression.members.all { isConstExpression(it.expression) }
        }
    }

    private fun isConstStatement(statement: BoundStatement): Boolean {
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