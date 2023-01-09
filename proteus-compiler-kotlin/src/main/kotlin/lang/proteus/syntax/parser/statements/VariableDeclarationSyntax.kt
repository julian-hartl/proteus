package lang.proteus.syntax.parser.statements

import lang.proteus.syntax.lexer.token.Keyword
import lang.proteus.syntax.lexer.token.Operator
import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Token
import lang.proteus.syntax.parser.ExpressionSyntax

internal class VariableDeclarationSyntax(
    val keyword: Keyword,
    val identifier: SyntaxToken<Token.Identifier>,
    val equalsToken: SyntaxToken<Operator.Equals>,
    val initializer: ExpressionSyntax,
) : StatementSyntax()