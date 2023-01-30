package lang.proteus.binding

import lang.proteus.symbols.FunctionSymbol
import lang.proteus.symbols.TypeSymbol

internal data class BoundCallExpression(val function: FunctionSymbol, val arguments: List<BoundExpression>) : BoundExpression() {
    override val type: TypeSymbol
        get() = function.returnType
}