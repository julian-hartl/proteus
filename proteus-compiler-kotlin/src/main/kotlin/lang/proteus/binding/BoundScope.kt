package lang.proteus.binding

import lang.proteus.symbols.FunctionSymbol
import lang.proteus.symbols.Symbol
import lang.proteus.symbols.VariableSymbol

internal class BoundScope internal constructor(val parent: BoundScope?) {
    private val variableScope: SymbolScope<VariableSymbol> = SymbolScope(parent?.variableScope)
    private val functionScope: SymbolScope<FunctionSymbol> = SymbolScope(parent?.functionScope)

    fun getDeclaredVariables(): List<VariableSymbol> = variableScope.getSymbols()

    fun tryDeclareVariable(variable: VariableSymbol): VariableSymbol? {
        return variableScope.tryDeclare(variable)
    }

    fun tryLookupVariable(variable: String): VariableSymbol? {
        return variableScope.tryLookup(variable)
    }

    fun getDeclaredFunctions(): List<FunctionSymbol> = functionScope.getSymbols()

    fun tryDeclareFunction(function: FunctionSymbol): FunctionSymbol? {
        return functionScope.tryDeclare(function)
    }

    fun tryLookupFunction(function: String): FunctionSymbol? {
        return functionScope.tryLookup(function)
    }

}

internal class SymbolScope<T : Symbol>(val parent: SymbolScope<T>?) {
    private val symbols: MutableMap<String, T> = mutableMapOf()

    fun getSymbols(): List<T> = symbols.values.toList()

    fun tryDeclare(variable: T): T? {
        val declaredVariable = symbols[variable.name]
        if (declaredVariable != null) {
            return null
        }

        symbols[variable.name] = variable
        return symbols[variable.name]
    }

    fun tryLookup(variable: String): T? {
        if (symbols.containsKey(variable)) {
            return symbols[variable]!!
        }
        if (parent == null) return null
        return parent.tryLookup(variable)

    }
}