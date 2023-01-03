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
        val boundOperatorKind = bindBinaryOperatorKind(binaryExpression, boundLeft.type, boundRight.type)
        if (boundOperatorKind == null) {
            diagnostics.add(
                "Operator ${binaryExpression.operatorToken.kind} cannot be applied to operands of type ${boundLeft.type} and ${boundRight.type}",
                binaryExpression.operatorToken.literal,
                binaryExpression.operatorToken.position
            )
            return boundLeft
        }
        return BoundBinaryExpression(
            boundLeft,
            boundRight,
            boundOperatorKind
        )
    }

    private fun bindBinaryOperatorKind(
        binaryExpression: BinaryExpression,
        leftType: KType,
        kType: KType
    ): BoundBinaryOperatorKind? {
        if (leftType != Int::class.createType() || kType != Int::class.createType()) {
            return null
        }
        return BoundBinaryOperatorKind.fromSyntaxToken(binaryExpression.operatorToken)
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
        if (operandType != Int::class.createType())
            return null;
        return BoundUnaryOperatorKind.fromSyntaxToken(unaryExpression.operatorToken)
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