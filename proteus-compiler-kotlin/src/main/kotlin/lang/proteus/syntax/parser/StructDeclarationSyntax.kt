package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Keyword
import lang.proteus.syntax.lexer.token.Token

internal class StructDeclarationSyntax(
    syntaxTree: SyntaxTree,
    val structToken: SyntaxToken<Keyword.Struct>,
    val identifier: SyntaxToken<Token.Identifier>,
    val openBraceToken: SyntaxToken<Token.OpenBrace>,
    val members: List<StructMemberSyntax>,
    val closeBraceToken: SyntaxToken<Token.CloseBrace>,

    ) : MemberSyntax(syntaxTree) {
    override val token: Token
        get() = Token.StructDeclaration
}

internal class StructMemberSyntax(
    syntaxTree: SyntaxTree,
    val mutabilityToken: SyntaxToken<Keyword.Mut>?,
    val identifier: SyntaxToken<Token.Identifier>,
    val type: TypeClauseSyntax,
    val semiColonToken: SyntaxToken<Token.SemiColon>,
) : SyntaxNode(syntaxTree) {
    override val token: Token
        get() = Token.StructMember
}