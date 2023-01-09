package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.token.Operator
import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Token

internal class AssignmentExpressionSyntax(
    val identifierToken: SyntaxToken<Token.Identifier>,
    val equalsToken: SyntaxToken<Operator.Equals>,
    val expression: ExpressionSyntax
) : ExpressionSyntax() {

}