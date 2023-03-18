package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Operator

internal class ReferenceExpressionSyntax internal constructor(
    val amper: SyntaxToken<Operator.Ampersand>,
    val expression: ExpressionSyntax,
    syntaxTree: SyntaxTree,
) : ExpressionSyntax(syntaxTree)