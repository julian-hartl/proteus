package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.Token


class NameExpressionSyntax(val identifierToken: SyntaxToken<Token.Identifier>) : ExpressionSyntax()