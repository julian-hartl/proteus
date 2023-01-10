package lang.proteus.binding

internal data class BoundForStatement(
    val variable: VariableSymbol,
    val lowerBound: BoundExpression,
    val upperBound: BoundExpression,
    val body: BoundStatement
) : BoundStatement()