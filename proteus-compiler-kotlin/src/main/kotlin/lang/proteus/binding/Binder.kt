package lang.proteus.binding

import lang.proteus.diagnostics.Diagnosable
import lang.proteus.diagnostics.DiagnosticsBag
import lang.proteus.syntax.parser.*

class Binder(private val variableContainer: VariableContainer = VariableContainer()) : Diagnosable {


    private val diagnosticsBag = DiagnosticsBag()

    override val diagnostics = diagnosticsBag.diagnostics

    fun bindSyntaxTree(tree: SyntaxTree): BoundExpression {
        return bind(tree.root)
    }

    fun bind(syntax: ExpressionSyntax): BoundExpression {
        return when (syntax) {
            is LiteralExpressionSyntax -> {
                bindLiteralExpression(syntax)
            }

            is UnaryExpressionSyntax -> {
                bindUnaryExpression(syntax)
            }

            is BinaryExpressionSyntax -> {
                bindBinaryExpression(syntax)
            }


            is ParenthesizedExpressionSyntax -> {
                bind(syntax.expressionSyntax)
            }

            is NameExpressionSyntax -> bindNameExpressionSyntax(syntax)
            is AssignmentExpressionSyntax -> bindAssignmentExpression(syntax)
        }
    }

    private fun bindAssignmentExpression(syntax: AssignmentExpressionSyntax): BoundExpression {
        val boundExpression = bind(syntax.expression)
        val variableName = syntax.identifierToken.literal
        val symbol = variableContainer.getVariableSymbol(variableName)
        val newType = boundExpression.type
        if (symbol != null && !variableContainer.canAssignTo(symbol, newType)) {
            diagnosticsBag.reportCannotAssign(syntax.equalsToken.span(), symbol.type, newType)
        }
        return BoundAssignmentExpression(VariableSymbol(variableName, newType), boundExpression)
    }

    private fun bindNameExpressionSyntax(syntax: NameExpressionSyntax): BoundExpression {
        val name = syntax.identifierToken.literal
        val variableSymbol = variableContainer.getVariableSymbol(name)
        val value = if (variableSymbol == null) null else variableContainer.getVariableValue(variableSymbol)
        if (value == null) {
            diagnosticsBag.reportUndefinedReference(syntax.identifierToken.span(), name)
            return BoundLiteralExpression(0)
        }
        val type = ProteusType.fromValueOrObject(value)
        return BoundVariableExpression(VariableSymbol(name, type))
    }


    private fun bindBinaryExpression(binaryExpression: BinaryExpressionSyntax): BoundExpression {

        val boundLeft = bind(binaryExpression.left)
        val boundRight = bind(binaryExpression.right)
        val binaryOperator =
            BoundBinaryOperator.bind(binaryExpression.operatorToken.token, boundLeft.type, boundRight.type)
        if (binaryOperator == null) {
            diagnosticsBag.reportBinaryOperatorMismatch(
                binaryExpression.operatorToken.literal,
                binaryExpression.operatorToken.span(),
                boundLeft.type,
                boundRight.type
            )
            return boundLeft
        }
        return BoundBinaryExpression(boundLeft, boundRight, binaryOperator)

    }


    private fun bindUnaryExpression(unaryExpression: UnaryExpressionSyntax): BoundExpression {
        val boundOperand = bind(unaryExpression.operand)
        val boundOperator = BoundUnaryOperator.bind(unaryExpression.operatorSyntaxToken.token, boundOperand.type)
        if (boundOperator == null) {
            diagnosticsBag.reportUnaryOperatorMismatch(
                unaryExpression.operatorSyntaxToken.literal,
                unaryExpression.operatorSyntaxToken.span(),
                boundOperand.type
            )
            return boundOperand
        }
        return BoundUnaryExpression(boundOperand, boundOperator)
    }


    private fun bindLiteralExpression(syntax: LiteralExpressionSyntax): BoundLiteralExpression<*> {
        val value = evaluateValueOfLiteralExpression(syntax)
        return BoundLiteralExpression(value)
    }

    private fun evaluateValueOfLiteralExpression(syntax: LiteralExpressionSyntax): Any {
        return syntax.value
    }


}