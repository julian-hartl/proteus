package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Keyword
import lang.proteus.syntax.lexer.token.Token

internal class ImportStatementSyntax(
    val importToken: SyntaxToken<Keyword.Import>,
    val filePath: SyntaxToken<Token.String>,
    val semiColon: SyntaxToken<Token.SemiColon>,
    val absolutePath: String,
    syntaxTree: SyntaxTree,
) : MemberSyntax(syntaxTree) {
    override val token: Token
        get() = Token.ImportStatement
}