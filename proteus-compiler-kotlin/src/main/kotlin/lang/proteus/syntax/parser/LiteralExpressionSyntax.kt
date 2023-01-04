package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.Token

class LiteralExpressionSyntax(private val syntaxToken: SyntaxToken<*>, val value: Any) : ExpressionSyntax() {
    override val token: Token
        get() = syntaxToken.token


    override fun getChildren(): Iterator<SyntaxNode> {
        return iterator {
            yield(syntaxToken)
        }
    }

}