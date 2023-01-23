package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Keyword
import lang.proteus.syntax.lexer.token.Token

internal class CastExpressionSyntax(
    val expressionSyntax: ExpressionSyntax,
    val asKeyword: SyntaxToken<Keyword.As>,
    val typeToken: SyntaxToken<Token.Type>, syntaxTree: SyntaxTree,
) :
    ExpressionSyntax(syntaxTree) {
}