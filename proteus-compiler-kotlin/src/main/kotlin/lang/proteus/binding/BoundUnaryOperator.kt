package lang.proteus.binding

import lang.proteus.symbols.TypeSymbol
import lang.proteus.syntax.lexer.token.Operator

internal sealed class BoundUnaryOperator(
    val operator: Operator,
    val operandTypes: Set<TypeSymbol>,
    val resultType: TypeSymbol,
) : BoundOperator() {

    constructor(operator: Operator, type: TypeSymbol) : this(
        operator,
        setOf(type),
        type
    )

    companion object {
        private val operators = listOf(
            BoundUnaryIdentityOperator,
            BoundUnaryNegationOperator,
            BoundUnaryNotOperator,
            BoundUnaryTypeOfOperator,
            BoundDereferenceOperator,
        )

        fun bind(operator: Operator, operandType: TypeSymbol): BoundUnaryOperator? {
            return operators.firstOrNull {
                it.operator == operator && it.operandTypes.any { symbol ->
                    operandType.isAssignableTo(symbol)
                }
            }
        }
    }

    object BoundUnaryNotOperator : BoundUnaryOperator(Operator.Not, TypeSymbol.Boolean)
    object BoundUnaryIdentityOperator : BoundUnaryOperator(Operator.Plus, TypeSymbol.Int)
    object BoundUnaryNegationOperator : BoundUnaryOperator(Operator.Minus, TypeSymbol.Int)

    object BoundUnaryTypeOfOperator : BoundUnaryOperator(Operator.TypeOf, setOf(TypeSymbol.Any), TypeSymbol.Type)

    object BoundDereferenceOperator :
        BoundUnaryOperator(Operator.Asterisk, setOf(
            TypeSymbol.Pointer(TypeSymbol.Any, true),
            TypeSymbol.Pointer(TypeSymbol.Any, false)
        ), TypeSymbol.Any)


}