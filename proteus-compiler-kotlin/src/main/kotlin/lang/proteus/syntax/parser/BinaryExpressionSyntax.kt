package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.Operator
import lang.proteus.syntax.lexer.SyntaxToken

class BinaryExpressionSyntax(
    val left: ExpressionSyntax,
    val operatorToken: SyntaxToken<Operator>,
    val right: ExpressionSyntax
) : ExpressionSyntax() {
    override val token: Operator
        get() = operatorToken.token


    override fun getChildren(): Iterator<SyntaxNode> {
        return iterator {
            yield(left)
            yield(operatorToken)
            yield(right)
        }
    }
}