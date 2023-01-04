package syntax.parser

import syntax.lexer.Token

abstract class SyntaxNode {
    abstract val token: Token
    abstract fun getChildren(): Iterator<SyntaxNode>
}