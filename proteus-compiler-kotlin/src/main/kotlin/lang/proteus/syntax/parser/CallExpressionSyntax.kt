package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Operator
import lang.proteus.syntax.lexer.token.Token


internal  class CallExpressionSyntax(
    val functionIdentifier: SyntaxToken<Token.Identifier>,
    val openParenthesis: SyntaxToken<Operator.OpenParenthesis>,
    val arguments: SeparatedSyntaxList<ExpressionSyntax>,
    val closeParenthesis: SyntaxToken<Operator.CloseParenthesis>, syntaxTree: SyntaxTree
) : ExpressionSyntax(syntaxTree)