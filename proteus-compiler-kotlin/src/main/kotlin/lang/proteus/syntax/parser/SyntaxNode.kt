package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.Token

abstract class SyntaxNode {
    abstract val token: Token
    abstract fun getChildren(): List<SyntaxNode>

    override fun toString(): String {
        return token::class.simpleName!!
    }
}