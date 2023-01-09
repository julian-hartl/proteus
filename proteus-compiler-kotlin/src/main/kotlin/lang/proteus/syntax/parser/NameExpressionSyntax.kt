package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.Token


internal class NameExpressionSyntax(val identifierToken: SyntaxToken<Token.Identifier>) : ExpressionSyntax()