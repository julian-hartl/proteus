package lang.proteus.binding

import lang.proteus.diagnostics.Diagnostics
import lang.proteus.symbols.FunctionSymbol
import lang.proteus.symbols.VariableSymbol

internal data class BoundGlobalScope(
    val previous: BoundGlobalScope?,
    val diagnostics: Diagnostics,
    val functions: List<FunctionSymbol>,
    val variables: List<VariableSymbol>,
    val statement: BoundStatement,
)