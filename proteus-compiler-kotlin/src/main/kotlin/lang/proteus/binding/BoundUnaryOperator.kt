package lang.proteus.binding

import lang.proteus.syntax.lexer.token.Operator
import lang.proteus.symbols.TypeSymbol
internal sealed class BoundUnaryOperator(
    val operator: Operator,
    val operandType: TypeSymbol,
    val resultType: TypeSymbol
): BoundOperator() {

    constructor(operator: Operator, type: TypeSymbol) : this(
        operator,
        type,
        type
    )

    companion object {
        private val operators = BoundUnaryOperator::class.sealedSubclasses.map { it.objectInstance!! }

        fun bind(operator: Operator, operandType: TypeSymbol): BoundUnaryOperator? {
            return operators.firstOrNull { it.operator == operator && it.operandType.isAssignableTo(operandType) }
        }
    }

    object BoundUnaryNotOperator : BoundUnaryOperator(Operator.Not, TypeSymbol.Boolean)
    object BoundUnaryIdentityOperator : BoundUnaryOperator(Operator.Plus, TypeSymbol.Int)
    object BoundUnaryNegationOperator : BoundUnaryOperator(Operator.Minus, TypeSymbol.Int)

    object BoundUnaryTypeOfOperator : BoundUnaryOperator(Operator.TypeOf, TypeSymbol.Any, TypeSymbol.Type)

}