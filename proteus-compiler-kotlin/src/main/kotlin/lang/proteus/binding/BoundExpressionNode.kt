package lang.proteus.binding

import lang.proteus.symbols.TypeSymbol

internal sealed class BoundExpression : BoundNode() {
    abstract val type: TypeSymbol
}

internal class BoundLiteralExpression<T : Any>(val value: T) : BoundExpression() {

    override val type: TypeSymbol
        get() = TypeSymbol.fromValueOrAny(value)
}

internal class BoundBinaryExpression(
    val left: BoundExpression,
    val right: BoundExpression,
    val operator: BoundBinaryOperator,
) : BoundExpression() {

    override val type: TypeSymbol
        get() = operator.resultType
}

internal class BoundUnaryExpression(val operand: BoundExpression, val operator: BoundUnaryOperator) :
    BoundExpression() {

    override val type: TypeSymbol
        get() = when (operator) {
            BoundUnaryOperator.BoundUnaryIdentityOperator -> operand.type
            BoundUnaryOperator.BoundUnaryNegationOperator -> operand.type
            BoundUnaryOperator.BoundUnaryNotOperator -> operand.type
            BoundUnaryOperator.BoundUnaryTypeOfOperator -> TypeSymbol.Type
            BoundUnaryOperator.BoundDereferenceOperator -> (operand.type as TypeSymbol.Pointer).type
            BoundUnaryOperator.BoundReferenceOperator -> TypeSymbol.Pointer(operand.type)
        }


}




