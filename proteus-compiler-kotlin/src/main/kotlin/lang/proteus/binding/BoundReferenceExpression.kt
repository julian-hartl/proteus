package lang.proteus.binding

import lang.proteus.symbols.TypeSymbol

internal class BoundReferenceExpression(
    val expression: BoundExpression,
    val isMutable: Boolean,
) : BoundExpression() {
    override val type: TypeSymbol
        get() = expression.type.ref(isMutable)
}