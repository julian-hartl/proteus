package lang.proteus.binding

internal data class BoundGotoStatement(val label: BoundLabel) : BoundStatement()