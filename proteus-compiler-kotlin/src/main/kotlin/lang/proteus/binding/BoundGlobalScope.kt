package lang.proteus.binding

import lang.proteus.diagnostics.Diagnostics
import lang.proteus.symbols.FunctionSymbol
import lang.proteus.symbols.GlobalVariableSymbol
import lang.proteus.symbols.VariableSymbol
import lang.proteus.syntax.parser.SyntaxTree

internal data class BoundGlobalScope(
    val previous: BoundGlobalScope?,
    val diagnostics: Diagnostics,
    val mappedFunctions: Map<SyntaxTree, Set<FunctionSymbol>>,
    val mappedVariables: Map<SyntaxTree, Set<VariableSymbol>>,
) {
    val functions: Set<FunctionSymbol>
        get() = mappedFunctions.flatMap { it.value }.distinctBy { it.qualifiedName }.toSet()
    val variables: Set<VariableSymbol>
        get() = mappedVariables.flatMap { it.value }.distinctBy { it.qualifiedName }.toSet()

    val globalVariables: Set<GlobalVariableSymbol>
        get() = variables.filter { it.isGlobal }.map { it as GlobalVariableSymbol }.toSet()
}