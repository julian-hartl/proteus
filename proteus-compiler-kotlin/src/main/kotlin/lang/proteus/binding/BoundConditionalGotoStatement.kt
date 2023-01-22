package lang.proteus.binding

internal data class BoundConditionalGotoStatement(
    val condition: BoundExpression,
    val label: BoundLabel,
    val jumpIfFalse: Boolean ,
) : BoundStatement()