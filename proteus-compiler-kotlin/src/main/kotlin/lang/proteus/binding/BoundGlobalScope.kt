package lang.proteus.binding

import lang.proteus.diagnostics.Diagnostics

internal class BoundGlobalScope(
    val previous: BoundGlobalScope?,
    val diagnostics: Diagnostics,
    val variables: List<VariableSymbol>,
    val statement: BoundStatement,
)