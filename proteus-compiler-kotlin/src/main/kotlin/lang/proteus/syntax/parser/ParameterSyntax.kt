package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Keyword
import lang.proteus.syntax.lexer.token.Token

internal class ParameterSyntax(
    val mutabilityToken: SyntaxToken<Keyword.Mut>?,
    val identifier: SyntaxToken<Token.Identifier>,
    val typeClause: TypeClauseSyntax, syntaxTree: SyntaxTree,
) : SyntaxNode(syntaxTree) {
    override val token: Token
        get() = Token.Parameter
}