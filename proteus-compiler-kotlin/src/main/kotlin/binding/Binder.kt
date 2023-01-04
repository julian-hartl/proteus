package binding

import diagnostics.Diagnosable
import diagnostics.Diagnostics
import syntax.lexer.SyntaxKind
import syntax.parser.*
import kotlin.reflect.KType
import kotlin.reflect.full.createType

class Binder : Diagnosable {


    private val diagnostics = Diagnostics()

    fun bindSyntaxTree(tree: SyntaxTree): BoundExpression {
        return bind(tree.root)
    }

    fun bind(syntax: ExpressionSyntax): BoundExpression {
        return when (syntax.kind) {
            SyntaxKind.LiteralExpression -> {
                bindLiteralExpression(syntax as LiteralExpressionSyntax)
            }

            SyntaxKind.UnaryExpression -> {
                bindUnaryExpression(syntax as UnaryExpressionSyntax)
            }

            SyntaxKind.BinaryExpression -> {
                bindBinaryExpression(syntax as BinaryExpression)
            }


            SyntaxKind.ParenthesizedExpression -> {
                bind((syntax as ParenthesizedExpressionSyntax).expressionSyntax)
            }

            else -> throw Exception("Unexpected syntax ${syntax.kind}")
        }
    }


    private fun bindBinaryExpression(binaryExpression: BinaryExpression): BoundExpression {

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
        val binaryOperator = BoundBinaryOperator.bind(binaryExpression.operatorToken.kind, boundLeft.type, boundRight.type)
        if(binaryOperator == null) {
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
        val boundOperator = BoundUnaryOperator.bind(unaryExpression.operatorToken.kind, boundOperand.type)
        if (boundOperator == null) {
            diagnostics.add(
                "Unary operator '${unaryExpression.operatorToken.literal}' cannot be applied to operand of type ${boundOperand.type}",
                unaryExpression.operatorToken.literal,
                unaryExpression.operatorToken.position
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