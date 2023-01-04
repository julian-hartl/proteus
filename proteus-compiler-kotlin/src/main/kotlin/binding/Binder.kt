package binding

import diagnostics.Diagnosable
import diagnostics.Diagnostics
import syntax.parser.*

class Binder : Diagnosable {


    private val diagnostics = Diagnostics()

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

        }
    }


    private fun bindBinaryExpression(binaryExpression: BinaryExpressionSyntax): BoundExpression {

        val boundLeft = bind(binaryExpression.left)
        val boundRight = bind(binaryExpression.right)
        if (boundLeft.type != boundRight.type) {
            diagnostics.add(
                "Binary operator ${binaryExpression.operatorToken.literal} can only be applied to values of the same type",
                binaryExpression.operatorToken.literal,
                binaryExpression.operatorToken.position
            )
            return boundLeft
        }
        val binaryOperator =
            BoundBinaryOperator.bind(binaryExpression.operatorToken.token, boundLeft.type, boundRight.type)
        if (binaryOperator == null) {
            diagnostics.add(
                "Binary operator ${binaryExpression.operatorToken.literal} is not defined for types ${boundLeft.type} and ${boundRight.type}",
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
                "Unary operator '${unaryExpression.operatorSyntaxToken.literal}' cannot be applied to operand of type ${boundOperand.type}",
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

    override fun printDiagnostics() {
        diagnostics.print()
    }

    override fun hasErrors(): Boolean {
        return diagnostics.size() > 0
    }

}