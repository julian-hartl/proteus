package syntax.parser

import syntax.lexer.Operator
import syntax.lexer.SyntaxToken
import syntax.lexer.Token

class UnaryExpressionSyntax(val operatorSyntaxToken: SyntaxToken<Operator>, val operand: ExpressionSyntax) : ExpressionSyntax() {
    override val token: Token
        get() = operatorSyntaxToken.token


    override fun getChildren(): Iterator<SyntaxNode> {
        return iterator {
            yield(operatorSyntaxToken)
            yield(operand)
        }
    }

}