package lang.proteus.binding

class BoundAssignmentExpression(val symbol: VariableSymbol, val expression: BoundExpression) : BoundExpression() {
    override val type: ProteusType
        get() = expression.type

}
