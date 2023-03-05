package lang.proteus.binding

import lang.proteus.symbols.FunctionSymbol
import lang.proteus.symbols.StructSymbol
import lang.proteus.symbols.Symbol
import lang.proteus.symbols.VariableSymbol
import lang.proteus.syntax.parser.SyntaxTree

internal class BoundScope internal constructor(val parent: BoundScope?) {
    private val variableScope: SymbolScope<VariableSymbol> = SymbolScope(parent?.variableScope)
    private val functionScope: SymbolScope<FunctionSymbol> = SymbolScope(parent?.functionScope)
    private val structScope: SymbolScope<StructSymbol> = SymbolScope(parent?.structScope)

    fun getDeclaredVariables(): Map<SyntaxTree, Set<VariableSymbol>> = variableScope.getAllSymbols()

    fun tryDeclareVariable(variable: VariableSymbol, syntaxTree: SyntaxTree): VariableSymbol? {
        return variableScope.tryDeclare(variable, syntaxTree)
    }

    fun tryLookupVariable(variable: String, syntaxTree: SyntaxTree): VariableSymbol? {
        return variableScope.tryLookup(variable, syntaxTree)
    }

    fun getDeclaredFunctions(

    ): Map<SyntaxTree, Set<FunctionSymbol>> = functionScope.getAllSymbols()


    fun tryDeclareFunction(function: FunctionSymbol, syntaxTree: SyntaxTree): FunctionSymbol? {
        return functionScope.tryDeclare(function, syntaxTree)
    }

    fun tryLookupFunction(function: String, syntaxTree: SyntaxTree): FunctionSymbol? {
        return functionScope.tryLookup(function, syntaxTree)
    }

    fun getDeclaredStructs(): Map<SyntaxTree, Set<StructSymbol>> = structScope.getAllSymbols()

    fun tryDeclareStruct(struct: StructSymbol, syntaxTree: SyntaxTree): StructSymbol? {
        return structScope.tryDeclare(struct, syntaxTree)
    }

    fun tryLookupStruct(struct: String, syntaxTree: SyntaxTree): StructSymbol? {
        return structScope.tryLookup(struct, syntaxTree)
    }

}

internal class SymbolScope<T : Symbol>(val parent: SymbolScope<T>?) {
    private val symbols: MutableMap<SyntaxTree, MutableMap<String, T>> = mutableMapOf()

    fun getSymbols(syntaxTree: SyntaxTree): MutableMap<String, T> = symbols.getOrPut(syntaxTree) { mutableMapOf() }

    fun getAllSymbols(): Map<SyntaxTree, Set<T>> {
        val allSymbols = mutableMapOf<SyntaxTree, Set<T>>()
        for (syntaxTree in symbols.keys) {
            allSymbols[syntaxTree] = symbols[syntaxTree]!!.values.toSet()
        }
        return allSymbols
    }


    fun tryDeclare(variable: T, syntaxTree: SyntaxTree): T? {
        val declaredVariable = getSymbols(syntaxTree)[variable.simpleName]
        if (declaredVariable != null) {
            return null
        }

        symbols[syntaxTree]!![variable.simpleName] = variable
        return variable
    }

    fun tryLookup(variable: String, syntaxTree: SyntaxTree): T? {
        if (getSymbols(syntaxTree).containsKey(variable)) {
            return symbols[syntaxTree]!![variable]!!
        }
        if (parent == null) return null
        return parent.tryLookup(variable, syntaxTree)

    }
}