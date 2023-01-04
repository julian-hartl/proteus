package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.Operator
import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.Token

class AssignmentExpressionSyntax(
    val identifierToken: SyntaxToken<Token.Identifier>,
    val equalsToken: SyntaxToken<Operator.Equals>,
    val expression: ExpressionSyntax
) : ExpressionSyntax() {
    override val token: Token
        get() = identifierToken.token

    override fun getChildren(): Iterator<SyntaxNode> {
        return iterator {
            yield(identifierToken)
            yield(equalsToken)
            yield(expression)
        }

    }
}