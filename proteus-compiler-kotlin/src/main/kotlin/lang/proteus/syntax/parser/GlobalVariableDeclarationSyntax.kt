package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Token
import lang.proteus.syntax.parser.statements.StatementSyntax
import lang.proteus.syntax.parser.statements.VariableDeclarationSyntax

internal class GlobalVariableDeclarationSyntax(
    val statement: VariableDeclarationSyntax,
    val semicolonToken: SyntaxToken<Token.SemiColon>, syntaxTree: SyntaxTree,
) : MemberSyntax(syntaxTree) {
    override val token: Token
        get() = Token.GlobalStatement
}