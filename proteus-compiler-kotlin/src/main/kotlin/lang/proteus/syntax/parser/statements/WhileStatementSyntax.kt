package lang.proteus.syntax.parser.statements

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Keyword
import lang.proteus.syntax.parser.ExpressionSyntax

internal class WhileStatementSyntax(
    val whileToken: SyntaxToken<Keyword.While>,
    val condition: ExpressionSyntax,
    val body: StatementSyntax,
) : StatementSyntax()