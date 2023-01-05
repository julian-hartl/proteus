package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.Token

class ParenthesizedExpressionSyntax(
    val openParenthesesToken: SyntaxToken<*>,
    val expressionSyntax: ExpressionSyntax,
    val closeParenthesisToken: SyntaxToken<*>
) : ExpressionSyntax()
