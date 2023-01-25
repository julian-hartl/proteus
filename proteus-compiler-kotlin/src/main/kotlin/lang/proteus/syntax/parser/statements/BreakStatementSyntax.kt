package lang.proteus.syntax.parser.statements

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Keyword
import lang.proteus.syntax.lexer.token.Token
import lang.proteus.syntax.parser.SyntaxTree

internal  class BreakStatementSyntax(
    val breakToken: SyntaxToken<Keyword.Break>, syntaxTree: SyntaxTree,
) : StatementSyntax(syntaxTree) {
}

