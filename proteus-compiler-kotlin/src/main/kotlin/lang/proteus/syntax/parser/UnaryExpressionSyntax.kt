package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.Operator
import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.Token

internal class UnaryExpressionSyntax(val operatorSyntaxToken: SyntaxToken<Operator>, val operand: ExpressionSyntax) : ExpressionSyntax()