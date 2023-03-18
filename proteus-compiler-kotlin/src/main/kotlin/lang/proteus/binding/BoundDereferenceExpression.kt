package lang.proteus.binding

internal class BoundDereferenceExpression(
    val expression: BoundExpression,
): BoundExpression() {
    override val type get() = expression.type.deref()
}