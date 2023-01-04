package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.Token

class IdentifierExpressionSyntax(val identifierToken: SyntaxToken<Token.Identifier>) : ExpressionSyntax() {
    override val token: Token
        get() = Token.Identifier

    override fun getChildren(): Iterator<SyntaxNode> {
        return iterator {
            yield(identifierToken)
        }
    }
}