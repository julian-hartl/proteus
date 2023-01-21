package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Token

class TypeClauseSyntax(val colonToken: SyntaxToken<Token.Colon>, val type: SyntaxToken<Token.Type>) : SyntaxNode() {
    override val token: Token
        get() = Token.TypeClause
}