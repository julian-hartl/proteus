package lang.proteus.generation

import lang.proteus.binding.*
import lang.proteus.evaluator.BinaryExpressionEvaluator
import lang.proteus.evaluator.UnaryExpressionEvaluator
import lang.proteus.syntax.lexer.token.Operator

internal class Optimizer private constructor() : BoundTreeRewriter() {
    companion object {
        fun optimize(statement: BoundBlockStatement): BoundBlockStatement {
            val optimizer = Optimizer()
            return optimizer.rewriteBlockStatement(statement)
        }
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
        val initializer = rewriteExpression(node.initializer)
        if (initializer == node.initializer) {
            return node
        }
        return BoundVariableDeclaration(node.variable, initializer)
    }

    override fun rewriteCallExpression(expression: BoundCallExpression): BoundExpression {
        val arguments = expression.arguments.map { rewriteExpression(it) }
        if (arguments == expression.arguments) {
            return expression
        }
        return BoundCallExpression(expression.functionSymbol, arguments, expression.isExternal)
    }

    override fun rewriteAssignmentExpression(node: BoundAssignmentExpression): BoundExpression {
        val expression = rewriteExpression(node.expression)
        if (expression == node.expression) {
            return node
        }
        return BoundAssignmentExpression(node.variable, expression, node.assignmentOperator, node.returnAssignment)
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
        val statements = node.statements.map { rewriteStatement(it) }
        if (statements == node.statements) {
            return node
        }
        return BoundBlockStatement(statements)
    }

    override fun rewriteConversionExpression(expression: BoundConversionExpression): BoundExpression {
        val rewrittenExpression = rewriteExpression(expression.expression)
        if (rewrittenExpression == expression) {
            return expression
        }
        return BoundConversionExpression(expression.type, expression, expression.conversion)
    }

    override fun rewriteExpressionStatement(statement: BoundExpressionStatement): BoundStatement {
        val rewrittenExpression = rewriteExpression(statement.expression)
        if (rewrittenExpression == statement.expression) {
            return statement
        }
        return BoundExpressionStatement(rewrittenExpression)
    }

}