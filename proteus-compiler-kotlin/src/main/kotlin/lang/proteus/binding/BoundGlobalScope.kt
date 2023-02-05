package lang.proteus.binding

import lang.proteus.diagnostics.Diagnostics
import lang.proteus.symbols.FunctionSymbol
import lang.proteus.symbols.GlobalVariableSymbol
import lang.proteus.symbols.VariableSymbol

internal data class BoundGlobalScope(
    val previous: BoundGlobalScope?,
    val diagnostics: Diagnostics,
    val mappedFunctions: Map<Module, Set<FunctionSymbol>>,
    val mappedVariables: Map<Module, Set<VariableSymbol>>,
) {

    val constantPool: ConstantPool = ConstantPool()

    fun importConstantPool(constantPool: ConstantPool) {
        for((variable, expression) in constantPool.getConstants()) {
            this.constantPool.add(variable, expression)
        }
    }
}


internal class ConstantPool {
    private val constants: MutableMap<VariableSymbol, BoundExpression> = mutableMapOf()

    fun getConstants(): Map<VariableSymbol, BoundExpression> = constants

    fun add(variable: VariableSymbol, expression: BoundExpression) {
        constants[variable] = expression
    }

}