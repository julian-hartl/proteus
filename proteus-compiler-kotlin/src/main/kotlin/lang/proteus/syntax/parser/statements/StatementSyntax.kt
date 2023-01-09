package lang.proteus.syntax.parser.statements

import lang.proteus.syntax.lexer.token.Token
import lang.proteus.syntax.parser.SyntaxNode

internal sealed class StatementSyntax : SyntaxNode() {
    override val token: Token
        get() = Token.Statement
}

