package lang.proteus.syntax.parser.statements

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Keyword
import lang.proteus.syntax.lexer.token.Token

internal data class ContinueStatementSyntax(
    val continueKeyword: SyntaxToken<Keyword.Continue>,
) : StatementSyntax() {
}