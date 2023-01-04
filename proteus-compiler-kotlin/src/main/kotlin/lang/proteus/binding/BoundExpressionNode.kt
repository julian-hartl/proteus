package lang.proteus.binding

import kotlin.reflect.full.createType

sealed class BoundExpression : BoundNode() {
    abstract val type: BoundType
}

internal class BoundLiteralExpression<T : Any>(val value: T) : BoundExpression() {

    override val type: BoundType
        get() = if (value is BoundType) BoundType.Type else BoundType.fromKotlinTypeOrObject(value::class.createType())
}

internal class BoundBinaryExpression(
    val left: BoundExpression,
    val right: BoundExpression,
    val operator: BoundBinaryOperator
) : BoundExpression() {

    override val type: BoundType
        get() = operator.resultType
}

internal class BoundUnaryExpression(val operand: BoundExpression, val operator: BoundUnaryOperator) :
    BoundExpression() {

    override val type: BoundType
        get() = operator.resultType

}




