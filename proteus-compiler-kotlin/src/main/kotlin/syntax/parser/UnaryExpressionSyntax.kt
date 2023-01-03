package syntax.parser

import syntax.lexer.SyntaxKind
import syntax.lexer.SyntaxToken

class UnaryExpressionSyntax(val operatorToken: SyntaxToken<*>, val operand: ExpressionSyntax) : ExpressionSyntax() {

    override val kind: SyntaxKind
        get() = SyntaxKind.UnaryExpression

    override fun getChildren(): Iterator<SyntaxNode> {
        return iterator {
            yield(operatorToken)
            yield(operand)
        }
    }

}