package lang.proteus.syntax.parser.statements

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Token
import lang.proteus.syntax.parser.SyntaxTree

internal class BlockStatementSyntax(
    val openBraceToken: SyntaxToken<Token.OpenBrace>,
    val statements: List<StatementSyntax>,
    val closeBraceToken: SyntaxToken<Token.CloseBrace>, syntaxTree: SyntaxTree,
) : StatementSyntax(syntaxTree)