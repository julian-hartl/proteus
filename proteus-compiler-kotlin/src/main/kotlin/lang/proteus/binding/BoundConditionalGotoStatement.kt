package lang.proteus.binding

internal data class BoundConditionalGotoStatement(
    val condition: BoundExpression,
    val label: LabelSymbol,
    val jumpIfFalse: Boolean = false,
) : BoundStatement()