package lang.proteus.binding

import lang.proteus.symbols.TypeSymbol

internal data class BoundReturnStatement(val expression: BoundExpression?, val returnType: TypeSymbol) : BoundStatement() {

}
