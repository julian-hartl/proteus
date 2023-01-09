package lang.proteus.binding

internal class BoundVariableDeclaration(val variable: VariableSymbol, val initializer: BoundExpression) :
    BoundStatement()
