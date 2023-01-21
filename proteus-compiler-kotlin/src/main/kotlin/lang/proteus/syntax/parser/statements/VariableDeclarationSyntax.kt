package lang.proteus.syntax.parser.statements

import lang.proteus.syntax.lexer.token.Keyword
import lang.proteus.syntax.lexer.token.Operator
import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Token
import lang.proteus.syntax.parser.ExpressionSyntax
import lang.proteus.syntax.parser.TypeClauseSyntax

internal class VariableDeclarationSyntax(
    val keyword: Keyword,
    val identifier: SyntaxToken<Token.Identifier>,
    val typeClauseSyntax: TypeClauseSyntax?,
    val equalsToken: SyntaxToken<Operator.Equals>,
    val initializer: ExpressionSyntax,
) : StatementSyntax()