package lang.proteus.binding

import lang.proteus.symbols.LocalVariableSymbol
import lang.proteus.symbols.TypeSymbol

internal data class BoundForStatement(
    val variable: LocalVariableSymbol,
    val lowerBound: BoundExpression,
    val upperBound: BoundExpression,
    val body: BoundStatement
) : BoundStatement() {
    override val type: TypeSymbol
        get() = variable.type
}