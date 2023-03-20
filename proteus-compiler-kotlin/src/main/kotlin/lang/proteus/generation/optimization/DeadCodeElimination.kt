package lang.proteus.generation.optimization

import lang.proteus.binding.*

internal class DeadCodeElimination : BoundTreeRewriter(), Optimizer {


    override fun optimize(statement: BoundBlockStatement): BoundBlockStatement {
        return rewriteBlockStatement(statement)
    }

    override fun optimizeExpression(expression: BoundExpression): BoundExpression {
        return rewriteExpression(expression)
    }

    override fun rewriteIfStatement(node: BoundIfStatement): BoundStatement {
        val condition = rewriteExpression(node.condition)
        val conditionValue = getConstantValue(condition)
        if (conditionValue == null) {
            val thenStatement = rewriteStatement(node.thenStatement)
            val elseStatement = node.elseStatement?.let { rewriteStatement(it) }
            if (thenStatement == node.thenStatement && elseStatement == node.elseStatement) {
                return node
            }
            return BoundIfStatement(condition, thenStatement, elseStatement)
        } else {
            conditionValue as Boolean
            return if (conditionValue) {
                rewriteStatement(node.thenStatement)
            } else {
                node.elseStatement?.let { rewriteStatement(it) } ?: BoundNopStatement()
            }
        }
    }

    override fun rewriteWhileStatement(node: BoundWhileStatement): BoundStatement {
        val condition = rewriteExpression(node.condition)
        val conditionValue = getConstantValue(condition)
        if (conditionValue == null) {
            val body = rewriteStatement(node.body)
            if (body == node.body) {
                return node
            }
            return BoundWhileStatement(condition, body)
        } else {
            conditionValue as Boolean
            return if (!conditionValue) {
                BoundNopStatement()
            } else {
                val body = rewriteStatement(node.body)
                if (body == node.body) {
                    return node
                }
                return BoundWhileStatement(condition, body)
            }
        }
    }

}