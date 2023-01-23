package lang.proteus.syntax.parser.statements

import lang.proteus.syntax.lexer.token.Token
import lang.proteus.syntax.parser.SyntaxNode
import lang.proteus.syntax.parser.SyntaxTree

internal sealed class StatementSyntax(syntaxTree: SyntaxTree) : SyntaxNode(syntaxTree) {
    override val token: Token
        get() = Token.Statement
}

