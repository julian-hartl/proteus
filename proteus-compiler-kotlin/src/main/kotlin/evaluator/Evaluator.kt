package evaluator

import lexer.SyntaxKind
import parser.*

class Evaluator(private val syntaxTree: SyntaxTree) {

    fun evaluate(): Any {
        return evaluateExpression(syntaxTree.root)
    }

    private fun evaluateExpression(syntax: ExpressionSyntax): Any {
        // This suppression is needed in order to compile.
        @Suppress("REDUNDANT_ELSE_IN_WHEN")
        return when (syntax) {
            is LiteralExpressionSyntax -> syntax.value
            is BinaryExpression -> {
                evaluateBinaryExpression(syntax)
            }

            is UnaryExpressionSyntax -> {
                evaluateUnaryExpression(syntax)
            }

            is ParenthesizedExpressionSyntax -> evaluateExpression(syntax.expressionSyntax)
            else -> throw Exception("Unexpected syntax ${syntax.kind}")
        }

    }

    private fun evaluateUnaryExpression(syntax: UnaryExpressionSyntax): Any {
        val operand = evaluateExpression(syntax.operand)
        return when (syntax.operatorToken.kind) {
            SyntaxKind.PlusToken -> operand as Int
            SyntaxKind.MinusToken -> -(operand as Int)
            else -> throw Exception("Unexpected unary operator ${syntax.operatorToken.kind}")
        }
    }

    private fun evaluateBinaryExpression(syntax: BinaryExpression): Any {
        val left = evaluateExpression(syntax.left) as Int
        val right = evaluateExpression(syntax.right) as Int
        return when (syntax.operatorToken.kind) {
            SyntaxKind.PlusToken -> left + right
            SyntaxKind.MinusToken -> left - right
            SyntaxKind.AsteriskToken -> left * right
            SyntaxKind.SlashToken -> left / right
            SyntaxKind.EqualityToken -> left == right
            SyntaxKind.AmpersandToken -> left and right
            SyntaxKind.PipeToken -> left or right
            else -> throw Exception("Unexpected binary operator ${syntax.operatorToken.kind}")
        }
    }

}