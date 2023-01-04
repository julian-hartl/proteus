package syntax.parser

import syntax.lexer.SyntaxToken
import syntax.lexer.Token

class LiteralExpressionSyntax(private val syntaxToken: SyntaxToken<*>, val value: Any) : ExpressionSyntax() {
    override val token: Token
        get() = syntaxToken.token


    override fun getChildren(): Iterator<SyntaxNode> {
        return iterator {
            yield(syntaxToken)
        }
    }

}