package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Token

internal class ParameterSyntax(
    val identifier: SyntaxToken<Token.Identifier>,
    val typeClause: TypeClauseSyntax,
) : SyntaxNode() {
    override val token: Token
        get() = Token.Parameter
}