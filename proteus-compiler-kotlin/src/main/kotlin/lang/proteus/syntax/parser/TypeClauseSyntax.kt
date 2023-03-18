package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Token

internal class TypeClauseSyntax(val colonToken: SyntaxToken<Token.Colon>, val type: TypeSyntax,
                       syntaxTree: SyntaxTree
) : SyntaxNode(syntaxTree) {
    override val token: Token
        get() = Token.TypeClause
}

