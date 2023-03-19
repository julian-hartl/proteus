package lang.proteus.binding

import lang.proteus.symbols.TypeSymbol
import lang.proteus.symbols.VariableSymbol
import lang.proteus.syntax.lexer.token.AssignmentOperator

internal sealed class BoundAssignee<T : BoundExpression>(
    val expression: T,
) {

    class BoundVariableAssignee(val variable: VariableSymbol) :
        BoundAssignee<BoundVariableExpression>(BoundVariableExpression(variable))

    class BoundMemberAssignee(member: BoundMemberAccessExpression) :
        BoundAssignee<BoundMemberAccessExpression>(member)

    class BoundDereferenceAssignee(dereferenced: BoundExpression, val referencing: BoundAssignee<out BoundExpression>) :
        BoundAssignee<BoundExpression>(dereferenced)


    companion object {
        fun fromExpression(expression: BoundExpression): BoundAssignee<out BoundExpression> {
            return when (expression) {
                is BoundVariableExpression -> BoundVariableAssignee(expression.variable)
                is BoundMemberAccessExpression -> BoundMemberAssignee(expression)
                is BoundUnaryExpression -> {
                    if (expression.operator is BoundUnaryOperator.BoundDereferenceOperator) {
                        BoundDereferenceAssignee(expression, fromExpression(expression.operand))
                    } else {
                        throw Exception("Unexpected expression: $expression")
                    }
                }

                else -> throw Exception("Unexpected expression: $expression")
            }
        }
    }

}

internal class BoundAssignmentExpression(
    val assignee: BoundAssignee<out BoundExpression>,
    val expression: BoundExpression,
    val assignmentOperator: AssignmentOperator,
    val returnAssignment: Boolean,
) : BoundExpression() {
    override val type: TypeSymbol
        get() = expression.type

}
