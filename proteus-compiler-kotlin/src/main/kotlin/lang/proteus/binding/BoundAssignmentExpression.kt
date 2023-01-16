package lang.proteus.binding

import lang.proteus.symbols.VariableSymbol
import lang.proteus.syntax.lexer.token.AssignmentOperator

internal class BoundAssignmentExpression(
    val variableSymbol: VariableSymbol,
    val expression: BoundExpression,
    val assignmentOperator: AssignmentOperator,
) : BoundExpression() {
    override val type: ProteusType
        get() = expression.type

}
