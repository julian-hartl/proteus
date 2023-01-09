package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Token


internal class NameExpressionSyntax(val identifierToken: SyntaxToken<Token.Identifier>) : ExpressionSyntax()