package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.Operator
import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.Token

internal class AssignmentExpressionSyntax(
    val identifierToken: SyntaxToken<Token.Identifier>,
    val equalsToken: SyntaxToken<Operator.Equals>,
    val expression: ExpressionSyntax
) : ExpressionSyntax() {

}