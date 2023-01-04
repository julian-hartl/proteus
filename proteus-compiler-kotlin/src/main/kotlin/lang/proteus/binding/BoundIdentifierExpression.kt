package lang.proteus.binding

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.Token

class BoundIdentifierExpression(identifierToken: SyntaxToken<Token.Identifier>) : BoundExpression() {
    override val type: ProteusType
        get() = ProteusType.Identifier

}
