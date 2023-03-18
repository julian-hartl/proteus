package lang.proteus.binding

import lang.proteus.symbols.TypeSymbol

internal class BoundReferenceExpression internal constructor(
    val expression: BoundExpression,
) : BoundExpression() {
    override val type: TypeSymbol
        get() = expression.type.ref()
}