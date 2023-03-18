package lang.proteus.emit

import lang.proteus.binding.*
import lang.proteus.symbols.FunctionSymbol

internal abstract class Emitter<Output>(val boundProgram: BoundProgram) {
    abstract fun generate(): Output

    abstract fun generateFunction(functionSymbol: FunctionSymbol)

    fun generateExpression(expression: BoundExpression) {
        when (expression) {
            is BoundAssignmentExpression -> generateAssignmentExpression(expression)
            is BoundBinaryExpression -> generateBinaryExpression(expression)
            is BoundCallExpression -> generateCallExpression(expression)
            is BoundConversionExpression -> generateConversionExpression(expression)
            is BoundLiteralExpression<*> -> generateLiteralExpression(expression)
            is BoundUnaryExpression -> generateUnaryExpression(expression)
            is BoundVariableExpression -> generateVariableExpression(expression)
            is BoundStructInitializationExpression -> generateStructInitializationExpression(expression)
            is BoundMemberAccessExpression -> generateMemberAccessExpression(expression)
            is BoundReferenceExpression -> generateReferenceExpression(expression)
            is BoundDereferenceExpression -> generateDereferenceExpression(expression)
            else -> throw Exception("Unexpected expression: $expression")
        }
    }

    protected abstract fun generateDereferenceExpression(expression: BoundDereferenceExpression)

    protected abstract fun generateReferenceExpression(expression: BoundReferenceExpression)

    protected abstract fun generateMemberAccessExpression(expression: BoundMemberAccessExpression)

    protected abstract fun generateStructInitializationExpression(expression: BoundStructInitializationExpression)


    fun generateStatement(statement: BoundStatement) {
        when (statement) {
            is BoundBlockStatement -> generateBlockStatement(statement)
            is BoundExpressionStatement -> generateExpressionStatement(statement)
            is BoundVariableDeclaration -> generateVariableDeclaration(statement)
            is BoundConditionalGotoStatement -> generateConditionalGotoStatement(statement)
            is BoundGotoStatement -> generateGotoStatement(statement)
            is BoundLabelStatement -> generateLabelStatement(statement)
            is BoundNopStatement -> generateNopStatement(statement)
            is BoundReturnStatement -> generateReturnStatement(statement)
            else -> throw Exception("Unexpected statement: $statement")
        }
    }

    fun generateBlockStatement(statement: BoundBlockStatement) {
        for (s in statement.statements) {
            generateStatement(s)
        }
    }

    fun generateExpressionStatement(statement: BoundExpressionStatement) {
        generateExpression(statement.expression)
    }

    abstract fun generateReturnStatement(statement: BoundReturnStatement)

    abstract fun generateAssignmentExpression(expression: BoundAssignmentExpression)

    abstract fun generateBinaryExpression(expression: BoundBinaryExpression)

    abstract fun generateCallExpression(expression: BoundCallExpression)

    abstract fun generateConversionExpression(expression: BoundConversionExpression)

    abstract fun generateLiteralExpression(expression: BoundLiteralExpression<*>)

    abstract fun generateUnaryExpression(expression: BoundUnaryExpression)

    abstract fun generateVariableExpression(expression: BoundVariableExpression)

    abstract fun generateVariableDeclaration(statement: BoundVariableDeclaration)

    abstract fun generateConditionalGotoStatement(statement: BoundConditionalGotoStatement)

    abstract fun generateGotoStatement(statement: BoundGotoStatement)

    abstract fun generateLabelStatement(statement: BoundLabelStatement)

    abstract fun generateNopStatement(statement: BoundNopStatement)


}