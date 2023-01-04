package lang.proteus.binding

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.Token

class BoundVariableExpression(
    val name: String,
    override val type: ProteusType,
) : BoundExpression() {


}
