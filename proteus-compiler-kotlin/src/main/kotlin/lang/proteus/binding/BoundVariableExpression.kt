package lang.proteus.binding

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.Token

class BoundVariableExpression(
    val symbol: VariableSymbol
) : BoundExpression() {
    override val type: ProteusType
        get() = symbol.type


}
