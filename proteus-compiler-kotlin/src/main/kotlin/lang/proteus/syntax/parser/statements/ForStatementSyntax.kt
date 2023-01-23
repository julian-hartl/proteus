package lang.proteus.syntax.parser.statements

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Keyword
import lang.proteus.syntax.lexer.token.Operator
import lang.proteus.syntax.lexer.token.Token
import lang.proteus.syntax.parser.ExpressionSyntax
import lang.proteus.syntax.parser.SyntaxTree

internal  class ForStatementSyntax(
    val forToken: SyntaxToken<Keyword.For>,
    val identifier: SyntaxToken<Token.Identifier>,
    val inToken: SyntaxToken<Keyword.In>,
    val lowerBound: ExpressionSyntax,
    val rangeOperator: SyntaxToken<Keyword.Until>,
    val upperBound: ExpressionSyntax,
    val body: StatementSyntax, syntaxTree: SyntaxTree,
) : StatementSyntax(syntaxTree)