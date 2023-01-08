package lang.proteus.binding

internal class BoundAssignmentExpression(val symbol: VariableSymbol, val expression: BoundExpression) : BoundExpression() {
    override val type: ProteusType
        get() = expression.type

}
