package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Token

internal class MemberAccessExpressionSyntax(
    syntaxTree: SyntaxTree,
    val expression: ExpressionSyntax,
    val dotToken: SyntaxToken<Token.Dot>,
    val identifier: SyntaxToken<Token.Identifier>,
    ) : ExpressionSyntax(syntaxTree) {
}