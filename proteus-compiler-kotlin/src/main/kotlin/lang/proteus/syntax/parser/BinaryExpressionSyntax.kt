package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.Operator
import lang.proteus.syntax.lexer.SyntaxToken

class BinaryExpressionSyntax(
    val left: ExpressionSyntax,
    val operatorToken: SyntaxToken<Operator>,
    val right: ExpressionSyntax
) : ExpressionSyntax()

