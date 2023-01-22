package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Token

internal data class CompilationUnitSyntax(
    val members: List<MemberSyntax>,
    val endOfFileToken: SyntaxToken<Token.EndOfFile>,
) :
    SyntaxNode() {
    override val token: Token
        get() = Token.Expression

}

