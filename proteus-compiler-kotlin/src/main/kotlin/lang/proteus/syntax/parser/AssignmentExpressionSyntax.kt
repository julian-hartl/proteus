package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.AssignmentOperator
import lang.proteus.syntax.lexer.token.Token

internal class AssignmentExpressionSyntax(
    val identifierToken: SyntaxToken<Token.Identifier>,
    val assignmentOperator: SyntaxToken<AssignmentOperator>,
    val expression: ExpressionSyntax, syntaxTree: SyntaxTree,
) : ExpressionSyntax(
    syntaxTree
) {

}