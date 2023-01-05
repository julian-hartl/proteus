package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.Token

sealed class ExpressionSyntax() : SyntaxNode() {
    override val token: Token
        get() = Token.Expression

    override fun toString(): String {
        return this::class.simpleName!!
    }
}