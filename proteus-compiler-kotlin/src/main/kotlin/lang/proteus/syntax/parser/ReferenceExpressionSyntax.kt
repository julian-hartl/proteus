package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Keyword
import lang.proteus.syntax.lexer.token.Operator

internal class ReferenceExpressionSyntax(
    val referenceToken: SyntaxToken<Operator.Ampersand>,
    val mutabilityToken: SyntaxToken<Keyword.Mut>?,
    val expression: ExpressionSyntax,
    syntaxTree: SyntaxTree,
): ExpressionSyntax (syntaxTree)