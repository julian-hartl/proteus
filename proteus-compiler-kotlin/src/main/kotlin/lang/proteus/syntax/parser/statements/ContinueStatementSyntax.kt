package lang.proteus.syntax.parser.statements

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Keyword
import lang.proteus.syntax.lexer.token.Token
import lang.proteus.syntax.parser.SyntaxTree

internal  class ContinueStatementSyntax(
    val continueKeyword: SyntaxToken<Keyword.Continue>, syntaxTree: SyntaxTree,
) : StatementSyntax(syntaxTree) {
}