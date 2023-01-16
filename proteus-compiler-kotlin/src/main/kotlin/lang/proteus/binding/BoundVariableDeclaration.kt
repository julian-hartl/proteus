package lang.proteus.binding

import lang.proteus.symbols.VariableSymbol

internal class BoundVariableDeclaration(val variable: VariableSymbol, val initializer: BoundExpression) :
    BoundStatement()
