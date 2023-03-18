package lang.proteus.binding

import lang.proteus.symbols.TypeSymbol
import lang.proteus.syntax.lexer.token.Operator

internal sealed class BoundUnaryOperator(
    val operator: Operator,
    val operandType: TypeSymbol,
    val resultType: TypeSymbol,
) : BoundOperator() {

    constructor(operator: Operator, type: TypeSymbol) : this(
        operator,
        type,
        type
    )

    companion object {
        private val operators = listOf(
            BoundUnaryIdentityOperator,
            BoundUnaryNegationOperator,
            BoundUnaryNotOperator,
            BoundUnaryTypeOfOperator,
            BoundReferenceOperator,
            BoundDereferenceOperator,
        )

        fun bind(operator: Operator, operandType: TypeSymbol): BoundUnaryOperator? {
            return operators.firstOrNull { it.operator == operator && operandType.isAssignableTo(it.operandType) }
        }
    }

    object BoundUnaryNotOperator : BoundUnaryOperator(Operator.Not, TypeSymbol.Boolean)
    object BoundUnaryIdentityOperator : BoundUnaryOperator(Operator.Plus, TypeSymbol.Int)
    object BoundUnaryNegationOperator : BoundUnaryOperator(Operator.Minus, TypeSymbol.Int)

    object BoundUnaryTypeOfOperator : BoundUnaryOperator(Operator.TypeOf, TypeSymbol.Any, TypeSymbol.Type)

    object BoundReferenceOperator : BoundUnaryOperator(Operator.Ampersand, TypeSymbol.Any, TypeSymbol.Pointer(TypeSymbol.Any))

    object BoundDereferenceOperator : BoundUnaryOperator(Operator.Asterisk, TypeSymbol.Pointer(TypeSymbol.Any), TypeSymbol.Any)

}