package lang.proteus.binding

import lang.proteus.symbols.FunctionSymbol
import lang.proteus.symbols.TypeSymbol

internal data class BoundCallExpression(val functionSymbol: FunctionSymbol, val arguments: List<BoundExpression>, val isExternal: Boolean) : BoundExpression() {
    override val type: TypeSymbol
        get() = functionSymbol.returnType
}