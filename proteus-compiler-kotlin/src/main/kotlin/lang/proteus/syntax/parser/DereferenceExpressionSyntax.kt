package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Operator

internal class DereferenceExpressionSyntax(
    val dereferenceOperator: SyntaxToken<Operator.Asterisk>,
    val expression: ExpressionSyntax,
    syntaxTree: SyntaxTree,
) : ExpressionSyntax(syntaxTree)