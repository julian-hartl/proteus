package parser

import lexer.SyntaxKind
import lexer.SyntaxToken

class NumberExpressionSyntax(private val numberToken: SyntaxToken<*>) : ExpressionSyntax() {
    override val kind: SyntaxKind
        get() = SyntaxKind.NumberExpression

    val value: Number
        get() = numberToken.value as Number

    override fun getChildren(): Iterator<SyntaxNode> {
        return iterator {
            yield(numberToken)
        }
    }

}