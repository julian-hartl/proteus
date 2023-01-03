package parser

import lexer.SyntaxKind
import lexer.SyntaxToken

class BinaryExpression (
    val left: ExpressionSyntax,
    val operatorToken: SyntaxToken<*>,
    val right: ExpressionSyntax
) : ExpressionSyntax() {


    override val kind: SyntaxKind
        get() = SyntaxKind.BinaryExpression

    override fun getChildren(): Iterator<SyntaxNode> {
        return iterator {
            yield(left)
            yield(operatorToken)
            yield(right)
        }
    }
}