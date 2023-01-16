package lang.proteus.binding

import lang.proteus.symbols.TypeSymbol

internal object BoundErrorExpression : BoundExpression() {
    override val type: TypeSymbol
        get() = TypeSymbol.Error
}