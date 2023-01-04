package lang.proteus.binding

import lang.proteus.diagnostics.Diagnosable
import lang.proteus.diagnostics.MutableDiagnostics
import lang.proteus.syntax.parser.*

class Binder : Diagnosable {


    override val diagnostics = MutableDiagnostics()

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

            is IdentifierExpressionSyntax -> bindIdentifierExpression(syntax)

        }
    }

    private fun bindIdentifierExpression(syntax: IdentifierExpressionSyntax): BoundExpression {
        return BoundIdentifierExpression(syntax.identifierToken)
    }


    private fun bindBinaryExpression(binaryExpression: BinaryExpressionSyntax): BoundExpression {

        val boundLeft = bind(binaryExpression.left)
        val boundRight = bind(binaryExpression.right)
        val binaryOperator =
            BoundBinaryOperator.bind(binaryExpression.operatorToken.token, boundLeft.type, boundRight.type)
        if (binaryOperator == null) {
            diagnostics.add(
                "Operator '${binaryExpression.operatorToken.literal}' cannot be applied to '${boundLeft.type}' and '${boundRight.type}'",
                binaryExpression.operatorToken.literal,
                binaryExpression.operatorToken.position
            )
            return boundLeft
        }
        return BoundBinaryExpression(boundLeft, boundRight, binaryOperator)

    }


    private fun bindUnaryExpression(unaryExpression: UnaryExpressionSyntax): BoundExpression {
        val boundOperand = bind(unaryExpression.operand)
        val boundOperator = BoundUnaryOperator.bind(unaryExpression.operatorSyntaxToken.token, boundOperand.type)
        if (boundOperator == null) {
            diagnostics.add(
                "Operator '${unaryExpression.operatorSyntaxToken.literal}' cannot be applied to '${boundOperand.type}'",
                unaryExpression.operatorSyntaxToken.literal,
                unaryExpression.operatorSyntaxToken.position
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