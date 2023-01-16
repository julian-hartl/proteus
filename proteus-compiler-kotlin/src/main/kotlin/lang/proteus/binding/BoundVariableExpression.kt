package lang.proteus.binding

import lang.proteus.symbols.VariableSymbol
import lang.proteus.symbols.TypeSymbol
internal class BoundVariableExpression(
    val symbol: VariableSymbol
) : BoundExpression() {
    override val type: TypeSymbol
        get() = symbol.type


}
