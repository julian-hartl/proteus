package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Keyword
import lang.proteus.syntax.lexer.token.Token

internal class StructInitializationExpressionSyntax(
    syntaxTree: SyntaxTree,
    val identifier: SyntaxToken<Token.Identifier>,
    val openBraceToken: SyntaxToken<Token.OpenBrace>,
    val members: SeparatedSyntaxList<StructMemberInitializationSyntax>,
    val closeBraceToken: SyntaxToken<Token.CloseBrace>,
) : ExpressionSyntax(syntaxTree)

internal class StructMemberInitializationSyntax(
    syntaxTree: SyntaxTree,
    val identifier: SyntaxToken<Token.Identifier>,
    val colonToken: SyntaxToken<Token.Colon>,
    val expression: ExpressionSyntax,
) : SyntaxNode(syntaxTree) {
    override val token: Token
        get() = Token.StructMemberInitialization
}