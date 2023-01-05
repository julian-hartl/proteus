package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.Operator
import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.Token

class AssignmentExpressionSyntax(
    val identifierToken: SyntaxToken<Token.Identifier>,
    val equalsToken: SyntaxToken<Operator.Equals>,
    val expression: ExpressionSyntax
) : ExpressionSyntax() {

    override fun getChildren(): List<SyntaxNode> {
        return listOf(
            identifierToken,
            equalsToken,
            expression
        )

    }
}