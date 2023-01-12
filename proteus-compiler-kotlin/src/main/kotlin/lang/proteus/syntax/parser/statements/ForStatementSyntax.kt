package lang.proteus.syntax.parser.statements

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Keyword
import lang.proteus.syntax.lexer.token.Token
import lang.proteus.syntax.parser.ExpressionSyntax

internal data class ForStatementSyntax(
    val forToken: SyntaxToken<Keyword.For>,
    val identifier: SyntaxToken<Token.Identifier>,
    val inToken: SyntaxToken<Keyword.In>,
    val iteratorExpression: ExpressionSyntax,
    val body: StatementSyntax,
) : StatementSyntax()