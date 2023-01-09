package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.token.Token

internal sealed class ExpressionSyntax() : SyntaxNode() {
    override val token: Token
        get() = Token.Expression

    override fun toString(): String {
        return this::class.simpleName!!
    }
}