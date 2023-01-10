package lang.proteus.binding

internal class BoundWhileStatement(val condition: BoundExpression, val body: BoundStatement) : BoundStatement()