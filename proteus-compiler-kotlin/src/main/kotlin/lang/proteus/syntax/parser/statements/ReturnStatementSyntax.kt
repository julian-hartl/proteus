package lang.proteus.syntax.parser.statements

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Keyword
import lang.proteus.syntax.parser.ExpressionSyntax
import lang.proteus.syntax.parser.SyntaxTree

internal  class ReturnStatementSyntax(
    val returnKeyword: SyntaxToken<Keyword.Return>,
    val expression: ExpressionSyntax?, syntaxTree: SyntaxTree,
) :
    StatementSyntax(syntaxTree)
