package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Keyword

internal class CastExpressionSyntax(
    val expressionSyntax: ExpressionSyntax,
    val asKeyword: SyntaxToken<Keyword.As>,
    val typeSyntax: TypeSyntax, syntaxTree: SyntaxTree,
) :
    ExpressionSyntax(syntaxTree) {
}