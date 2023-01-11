package lang.proteus.binding

internal data class BoundForStatement(
    val variable: VariableSymbol,
    val boundIteratorExpression: BoundExpression,
    val body: BoundStatement
) : BoundStatement()