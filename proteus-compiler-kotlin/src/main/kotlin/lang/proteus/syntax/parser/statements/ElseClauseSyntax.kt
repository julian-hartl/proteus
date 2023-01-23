package lang.proteus.syntax.parser.statements

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Keyword
import lang.proteus.syntax.lexer.token.Token
import lang.proteus.syntax.parser.SyntaxNode
import lang.proteus.syntax.parser.SyntaxTree

internal class ElseClauseSyntax(val elseKeyword: SyntaxToken<Keyword.Else>, val elseStatementSyntax: StatementSyntax,
                                syntaxTree: SyntaxTree
) :
    SyntaxNode(syntaxTree) {
    override val token: Token
        get() = Token.ElseClause
}