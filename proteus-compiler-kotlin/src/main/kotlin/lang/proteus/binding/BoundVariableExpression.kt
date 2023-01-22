package lang.proteus.binding

import lang.proteus.symbols.VariableSymbol
import lang.proteus.symbols.TypeSymbol
internal class BoundVariableExpression(
    val variable: VariableSymbol
) : BoundExpression() {
    override val type: TypeSymbol
        get() = variable.type


}
