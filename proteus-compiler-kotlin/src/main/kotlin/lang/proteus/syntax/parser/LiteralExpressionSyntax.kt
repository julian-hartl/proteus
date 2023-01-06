package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.Token

class LiteralExpressionSyntax(val syntaxToken: SyntaxToken<*>, val value: Any) : ExpressionSyntax()

