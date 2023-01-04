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
        val genericBinaryExpression = tryBindGenericBinaryExpression(boundLeft, boundRight, binaryExpression)
        if (genericBinaryExpression != null) {
            return genericBinaryExpression
        }
        return when (boundLeft.type) {
            Int::class.createType() -> {
                bindArithmeticBinaryExpression(boundLeft, boundRight, binaryExpression)
            }

            Boolean::class.createType() -> {
                bindLogicalBinaryExpression(boundLeft, boundRight, binaryExpression)
            }

            else -> {
                diagnostics.add(
                    "Binary operator ${binaryExpression.operatorToken.literal} can only be applied to values of type int or boolean",
                    binaryExpression.operatorToken.literal,
                    binaryExpression.operatorToken.position
                )
                boundLeft
            }
        }

    }

    private fun tryBindGenericBinaryExpression(
        boundLeft: BoundExpression,
        boundRight: BoundExpression,
        binaryExpression: BinaryExpression,
    ): BoundExpression? {
        val operatorKind = BoundGenericBinaryOperatorKind.fromSyntaxToken(binaryExpression.operatorToken) ?: return null
        return BoundGenericBinaryExpression(boundLeft, boundRight, operatorKind)
    }

    private fun bindLogicalBinaryExpression(
        boundLeft: BoundExpression,
        boundRight: BoundExpression,
        binaryExpression: BinaryExpression
    ): BoundExpression {
        val operatorKind = BoundBooleanBinaryOperatorKind.fromSyntaxToken(binaryExpression.operatorToken)
        if (operatorKind == null) {
            diagnostics.add(
                "Operator '${binaryExpression.operatorToken.literal}' cannot be applied to boolean values",
                binaryExpression.operatorToken.literal,
                binaryExpression.operatorToken.position
            )
            return boundLeft
        }
        return BoundBooleanBinaryExpression(boundLeft, boundRight, operatorKind)
    }

    private fun bindArithmeticBinaryExpression(
        boundLeft: BoundExpression,
        boundRight: BoundExpression,
        binaryExpression: BinaryExpression,
    ): BoundExpression {
        val boundOperatorKind = BoundNumberBinaryOperatorKind.fromSyntaxToken(binaryExpression.operatorToken)
        if (boundOperatorKind == null) {
            diagnostics.add(
                "Operator '${binaryExpression.operatorToken.literal}' cannot be applied to int values",
                binaryExpression.operatorToken.literal,
                binaryExpression.operatorToken.position
            )
            return boundLeft
        }
        return BoundNumberBinaryExpression(
            boundLeft,
            boundRight,
            boundOperatorKind
        )
    }


    private fun bindUnaryExpression(unaryExpression: UnaryExpressionSyntax): BoundExpression {
        val boundOperand = bind(unaryExpression.operand)
        val boundOperatorKind = bindUnaryOperatorKind(unaryExpression, boundOperand.type)
        if (boundOperatorKind == null) {
            diagnostics.add(
                "Unary operator '${unaryExpression.operatorToken.literal}' cannot be applied to operand of type ${boundOperand.type}",
                unaryExpression.operatorToken.literal,
                unaryExpression.operatorToken.position
            )
            return boundOperand
        }
        return BoundUnaryExpression(boundOperand, boundOperatorKind)
    }

    private fun bindUnaryOperatorKind(
        unaryExpression: UnaryExpressionSyntax,
        operandType: KType
    ): BoundUnaryOperatorKind? {
        return BoundUnaryOperatorKind.fromSyntaxToken(unaryExpression.operatorToken, operandType)
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