package lang.proteus.syntax.parser.statements

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Keyword
import lang.proteus.syntax.parser.ExpressionSyntax

internal class IfStatementSyntax(
    val ifKeyword: SyntaxToken<Keyword.If>,
    val condition: ExpressionSyntax,
    val thenStatement: StatementSyntax,
    val elseClause: ElseClauseSyntax?
) : StatementSyntax()

