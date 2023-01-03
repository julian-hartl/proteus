package parser

import lexer.SyntaxKind
import lexer.SyntaxToken

class LiteralExpressionSyntax(private val numberToken: SyntaxToken<*>) : ExpressionSyntax() {
    override val kind: SyntaxKind
        get() = SyntaxKind.LiteralExpression

    val value: Any
        get() = numberToken.value as Any

    override fun getChildren(): Iterator<SyntaxNode> {
        return iterator {
            yield(numberToken)
        }
    }

}