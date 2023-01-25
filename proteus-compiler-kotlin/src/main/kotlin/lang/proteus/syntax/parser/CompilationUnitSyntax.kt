package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Token

internal class CompilationUnitSyntax(
    val members: List<MemberSyntax>,
    val endOfFileToken: SyntaxToken<Token.EndOfFile>, syntaxTree: SyntaxTree,
) :
    SyntaxNode(syntaxTree) {
    override val token: Token
        get() = Token.Expression

}

