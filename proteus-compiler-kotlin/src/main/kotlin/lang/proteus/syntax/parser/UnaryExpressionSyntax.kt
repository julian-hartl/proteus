package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.Operator
import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.Token

class UnaryExpressionSyntax(val operatorSyntaxToken: SyntaxToken<Operator>, val operand: ExpressionSyntax) : ExpressionSyntax() {
    override val token: Token
        get() = operatorSyntaxToken.token


    override fun getChildren(): List<SyntaxNode> {
        return listOf(
            operatorSyntaxToken,
            operand
        )
    }

}