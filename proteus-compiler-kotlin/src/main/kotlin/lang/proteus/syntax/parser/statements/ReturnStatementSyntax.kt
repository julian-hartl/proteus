package lang.proteus.syntax.parser.statements

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Keyword
import lang.proteus.syntax.parser.ExpressionSyntax

internal data class ReturnStatementSyntax(
    val returnKeyword: SyntaxToken<Keyword.Return>,
    val expression: ExpressionSyntax?,
) :
    StatementSyntax()
