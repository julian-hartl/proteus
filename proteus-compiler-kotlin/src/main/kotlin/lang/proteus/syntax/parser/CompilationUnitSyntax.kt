package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.Token
import lang.proteus.syntax.parser.statements.StatementSyntax

internal class CompilationUnitSyntax(
    val statement: StatementSyntax,
    val endOfFileToken: SyntaxToken<Token.EndOfFile>,
) :
    SyntaxNode() {
    override val token: Token
        get() = Token.Expression

}

