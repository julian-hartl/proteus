package lang.proteus.binding

import lang.proteus.diagnostics.Diagnostics
import lang.proteus.symbols.FunctionSymbol
import lang.proteus.symbols.VariableSymbol
import lang.proteus.syntax.parser.SyntaxTree

internal data class BoundGlobalScope(
    val previous: BoundGlobalScope?,
    val diagnostics: Diagnostics,
    val functions: Map<SyntaxTree, Set<FunctionSymbol>>,
    val variables: Map<SyntaxTree, Set<VariableSymbol>>,
)