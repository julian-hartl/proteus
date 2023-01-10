package lang.proteus.binding

internal class BoundIfStatement(
    val condition: BoundExpression,
    val thenStatement: BoundStatement,
    val elseStatement: BoundStatement?,
) :
    BoundStatement() {

}
