package lang.proteus.binding

import lang.proteus.syntax.lexer.Token
import lang.proteus.syntax.parser.NameExpressionSyntax

class BoundAssignmentExpression(val identifierName: String, val expression: BoundExpression) : BoundExpression() {
    override val type: ProteusType
        get() = expression.type

}
