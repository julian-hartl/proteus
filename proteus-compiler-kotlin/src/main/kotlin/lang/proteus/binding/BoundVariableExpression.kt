package lang.proteus.binding

internal class BoundVariableExpression(
    val symbol: VariableSymbol
) : BoundExpression() {
    override val type: ProteusType
        get() = symbol.type


}
