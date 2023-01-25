package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.token.Operator
import lang.proteus.syntax.lexer.SyntaxToken

internal class UnaryExpressionSyntax(val operatorSyntaxToken: SyntaxToken<Operator>, val operand: ExpressionSyntax,
                                     syntaxTree: SyntaxTree
) : ExpressionSyntax(syntaxTree)