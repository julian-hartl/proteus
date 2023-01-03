package syntax.parser

import syntax.lexer.SyntaxKind

abstract class SyntaxNode {
    abstract val kind: SyntaxKind

    abstract fun getChildren(): Iterator<SyntaxNode>
}