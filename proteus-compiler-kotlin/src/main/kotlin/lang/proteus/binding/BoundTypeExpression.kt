package lang.proteus.binding

import lang.proteus.symbols.Symbol
import lang.proteus.symbols.TypeSymbol

internal class BoundTypeExpression(
    val symbol: Symbol
) : BoundExpression() {
    override val type: TypeSymbol
        get() = TypeSymbol.Type
}