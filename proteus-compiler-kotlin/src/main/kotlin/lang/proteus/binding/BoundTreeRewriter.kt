package lang.proteus.binding

internal abstract class BoundTreeRewriter {

    fun rewriteStatement(statement: BoundStatement): BoundStatement {
        return when (statement) {
            is BoundBlockStatement -> rewriteBlockStatement(statement)
            is BoundExpressionStatement -> rewriteExpressionStatement(statement)
            is BoundForStatement -> rewriteForStatement(statement)
            is BoundIfStatement -> rewriteIfStatement(statement)
            is BoundVariableDeclaration -> rewriteVariableDeclaration(statement)
            is BoundWhileStatement -> rewriteWhileStatement(statement)
            is BoundConditionalGotoStatement -> rewriteConditionalGotoStatement(statement)
            is BoundGotoStatement -> rewriteGotoStatement(statement)
            is BoundLabelStatement -> rewriteLabelStatement(statement)
            is BoundNopStatement -> rewriteNopStatement(statement)
            is BoundBreakStatement -> rewriteBreakStatement(statement)
            is BoundContinueStatement -> rewriteContinueStatement(statement)
            is BoundReturnStatement -> rewriteReturnStatement(statement)
        }
    }

    protected open fun rewriteReturnStatement(statement: BoundReturnStatement): BoundStatement {
        val expression = statement.expression?.let { rewriteExpression(it) }
        if (expression == statement.expression) {
            return statement
        }
        return BoundReturnStatement(expression, statement.returnType)
    }

    protected open fun rewriteContinueStatement(statement: BoundContinueStatement): BoundStatement {
        return statement
    }

    protected open fun rewriteBreakStatement(statement: BoundBreakStatement): BoundStatement {
        return statement
    }


    protected open fun rewriteNopStatement(statement: BoundNopStatement): BoundStatement {
        return statement
    }

    protected open fun rewriteLabelStatement(statement: BoundLabelStatement): BoundStatement {
        return statement
    }

    protected open fun rewriteGotoStatement(statement: BoundGotoStatement): BoundStatement {
        return statement
    }

    protected open fun rewriteConditionalGotoStatement(statement: BoundConditionalGotoStatement): BoundStatement {
        val condition = rewriteExpression(statement.condition)
        if (condition == statement.condition) {
            return statement
        }
        return BoundConditionalGotoStatement(condition, statement.label, statement.jumpIfFalse)
    }

    protected open fun rewriteWhileStatement(node: BoundWhileStatement): BoundStatement {
        val condition = rewriteExpression(node.condition)
        val body = rewriteStatement(node.body)
        if (condition == node.condition && body == node.body) {
            return node
        }
        return BoundWhileStatement(condition, body)
    }

    protected open fun rewriteVariableDeclaration(node: BoundVariableDeclaration): BoundStatement {
        val initializer = rewriteExpression(node.initializer)
        if (initializer == node.initializer) {
            return node
        }
        return BoundVariableDeclaration(node.variable, initializer)
    }

    protected open fun rewriteIfStatement(node: BoundIfStatement): BoundStatement {
        val condition = rewriteExpression(node.condition)
        val thenStatement = rewriteStatement(node.thenStatement)
        val elseStatement = node.elseStatement?.let { rewriteStatement(it) }
        if (condition == node.condition && thenStatement == node.thenStatement && elseStatement == node.elseStatement) {
            return node
        }
        return BoundIfStatement(condition, thenStatement, elseStatement)
    }

    protected open fun rewriteForStatement(node: BoundForStatement): BoundStatement {
        val lowerBound = rewriteExpression(node.lowerBound)
        val upperBound = rewriteExpression(node.upperBound)
        val body = rewriteStatement(node.body)
        if (lowerBound == node.lowerBound && upperBound == node.upperBound && body == node.body) {
            return node
        }
        return BoundForStatement(node.variable, lowerBound, node.rangeOperator, upperBound, body)
    }

    protected open fun rewriteExpressionStatement(statement: BoundExpressionStatement): BoundStatement {
        val expression = rewriteExpression(statement.expression)
        if (expression == statement.expression) {
            return statement
        }
        return BoundExpressionStatement(expression)
    }

    protected open fun rewriteBlockStatement(
        node: BoundBlockStatement,
    ): BoundBlockStatement {
        val statements = mutableListOf<BoundStatement>()
        var changed = false
        for (s in node.statements) {
            val rewritten = rewriteStatement(s)
            statements.add(rewritten)
            if (rewritten != s) {
                changed = true
            }
        }

        if (!changed) {
            return node
        }
        return BoundBlockStatement(statements)
    }

    fun rewriteExpression(expression: BoundExpression): BoundExpression {
        return when (expression) {
            is BoundAssignmentExpression -> rewriteAssignmentExpression(expression)
            is BoundBinaryExpression -> rewriteBinaryExpression(expression)
            is BoundLiteralExpression<*> -> rewriteLiteralExpression(expression)
            is BoundUnaryExpression -> rewriteUnaryExpression(expression)
            is BoundVariableExpression -> rewriteVariableExpression(expression)
            is BoundErrorExpression -> rewriteErrorExpression(expression)
            is BoundCallExpression -> rewriteCallExpression(expression)
            is BoundConversionExpression -> rewriteConversionExpression(expression)
            is BoundStructInitializationExpression -> rewriteStructInitializationExpression(expression)
            is BoundMemberAccessExpression -> rewriteMemberAccessExpression(expression)
            is BoundReferenceExpression -> rewriteReferenceExpression(expression)
            is BoundDereferenceExpression -> rewriteDereferenceExpression(expression)
        }
    }

    protected open fun rewriteDereferenceExpression(expression: BoundDereferenceExpression): BoundExpression {
        val expr = rewriteExpression(expression.expression)
        if (expr != expression.expression) {
            return BoundDereferenceExpression(expr)
        }
        return expression
    }

    protected open fun rewriteReferenceExpression(expression: BoundReferenceExpression): BoundExpression {
        val rewritten = rewriteExpression(expression.expression)
        if (rewritten == expression.expression) {
            return expression
        }
        return BoundReferenceExpression(rewritten)
    }

    private fun rewriteMemberAccessExpression(expression: BoundMemberAccessExpression): BoundExpression {
        val expr = rewriteExpression(expression.expression)
        if (expr != expression.expression) {
            return BoundMemberAccessExpression(expr, expression.memberName, expression.type)
        }
        return expression
    }

    protected open fun rewriteStructInitializationExpression(expression: BoundStructInitializationExpression): BoundExpression {
        val arguments = expression.members.map {
            val expr = rewriteExpression(it.expression)
            if (expr != it.expression) {
                BoundStructMemberInitializationExpression(it.name, expr)
            } else {
                it
            }

        }
        if (arguments == expression.members) {
            return expression
        }
        return BoundStructInitializationExpression(expression.struct, arguments)
    }

    protected open fun rewriteConversionExpression(expression: BoundConversionExpression): BoundExpression {
        val rewritten = rewriteExpression(expression.expression)
        if (expression.expression == rewritten) {
            return expression
        }
        return BoundConversionExpression(expression.type, rewritten, expression.conversion)
    }

    protected open fun rewriteCallExpression(expression: BoundCallExpression): BoundExpression {
        val arguments = expression.arguments.map { rewriteExpression(it) }
        if (arguments == expression.arguments) {
            return expression
        }
        return BoundCallExpression(expression.function, arguments)
    }

    private fun rewriteErrorExpression(expression: BoundExpression): BoundExpression {
        return expression
    }

    protected open fun rewriteBinaryExpression(node: BoundBinaryExpression): BoundExpression {
        val left = rewriteExpression(node.left)
        val right = rewriteExpression(node.right)
        if (left == node.left && right == node.right) {
            return node
        }
        return BoundBinaryExpression(left, right, node.operator)
    }

    protected open fun rewriteUnaryExpression(node: BoundUnaryExpression): BoundExpression {
        val expression = rewriteExpression(node.operand)
        if (expression == node.operand) {
            return node
        }
        return BoundUnaryExpression(expression, node.operator)
    }

    protected open fun rewriteVariableExpression(expression: BoundVariableExpression): BoundExpression {
        return expression
    }

    protected open fun rewriteLiteralExpression(expression: BoundLiteralExpression<*>): BoundExpression {
        return expression
    }


    protected open fun rewriteAssignmentExpression(node: BoundAssignmentExpression): BoundExpression {
        val expression = rewriteExpression(node.expression)
        if (expression == node.expression)
            return node
        return BoundAssignmentExpression(node.variable, expression, node.assignmentOperator, node.returnAssignment)
    }

}