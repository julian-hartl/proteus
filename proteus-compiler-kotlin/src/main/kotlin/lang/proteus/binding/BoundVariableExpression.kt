package lang.proteus.binding

import lang.proteus.symbols.VariableSymbol

internal class BoundVariableExpression(
    val symbol: VariableSymbol
) : BoundExpression() {
    override val type: ProteusType
        get() = symbol.type


}
