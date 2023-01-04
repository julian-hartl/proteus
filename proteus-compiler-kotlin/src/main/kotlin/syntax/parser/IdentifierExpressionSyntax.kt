package syntax.parser

import syntax.lexer.SyntaxToken
import syntax.lexer.Token

class IdentifierExpressionSyntax(val identifierToken: SyntaxToken<Token.Identifier>) : ExpressionSyntax() {
    override val token: Token
        get() = Token.Identifier

    override fun getChildren(): Iterator<SyntaxNode> {
        return iterator {
            yield(identifierToken)
        }
    }
}