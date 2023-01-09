package lang.proteus.binding

internal class BoundBlockStatement(
    val statements: List<BoundStatement>,
) : BoundStatement()