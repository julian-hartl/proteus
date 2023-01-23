package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.token.Operator
import lang.proteus.syntax.lexer.SyntaxToken

internal class BinaryExpressionSyntax(
    val left: ExpressionSyntax,
    val operatorToken: SyntaxToken<Operator>,
    val right: ExpressionSyntax, syntaxTree: SyntaxTree
) : ExpressionSyntax(syntaxTree)

