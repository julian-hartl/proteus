package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.SyntaxToken

internal class LiteralExpressionSyntax(val syntaxToken: SyntaxToken<*>, val value: Any, syntaxTree: SyntaxTree) : ExpressionSyntax(
    syntaxTree
)

