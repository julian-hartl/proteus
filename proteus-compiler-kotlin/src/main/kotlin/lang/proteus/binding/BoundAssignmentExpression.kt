package lang.proteus.binding

import lang.proteus.symbols.VariableSymbol
import lang.proteus.symbols.TypeSymbol
import lang.proteus.syntax.lexer.token.AssignmentOperator

internal class BoundAssignmentExpression(
    val variable: VariableSymbol,
    val expression: BoundExpression,
    val assignmentOperator: AssignmentOperator,
    val returnAssignment: Boolean ,
) : BoundExpression() {
    override val type: TypeSymbol
        get() = expression.type

}
