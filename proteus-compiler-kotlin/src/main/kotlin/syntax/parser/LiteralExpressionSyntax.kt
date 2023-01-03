package syntax.parser

import syntax.lexer.SyntaxKind
import syntax.lexer.SyntaxToken

class LiteralExpressionSyntax(private val numberToken: SyntaxToken<*>, val value: Any) : ExpressionSyntax() {
    override val kind: SyntaxKind
        get() = SyntaxKind.LiteralExpression


    override fun getChildren(): Iterator<SyntaxNode> {
        return iterator {
            yield(numberToken)
        }
    }

}