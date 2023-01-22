package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Token
import lang.proteus.syntax.parser.statements.StatementSyntax

internal class GlobalStatementSyntax(
    val statement: StatementSyntax,
    val semicolonToken: SyntaxToken<Token.SemiColon>?,
) : MemberSyntax() {
    override val token: Token
        get() = Token.GlobalStatement
}