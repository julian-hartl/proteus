package binding

import syntax.lexer.SyntaxToken
import syntax.lexer.Token

class BoundIdentifierExpression(identifierToken: SyntaxToken<Token.Identifier>) : BoundExpression() {
    override val type: BoundType
        get() = BoundType.Identifier

}
