package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.Token

internal class ParenthesizedExpressionSyntax(
    val openParenthesesToken: SyntaxToken<*>,
    val expressionSyntax: ExpressionSyntax,
    val closeParenthesisToken: SyntaxToken<*>
) : ExpressionSyntax()
