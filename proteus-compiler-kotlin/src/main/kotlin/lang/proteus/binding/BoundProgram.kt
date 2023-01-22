package lang.proteus.binding

import lang.proteus.diagnostics.Diagnostics
import lang.proteus.symbols.FunctionSymbol

internal data class BoundProgram(
    val globalScope: BoundGlobalScope,
    val diagnostics: Diagnostics,
    val functionBodies: Map<FunctionSymbol, BoundBlockStatement>,
)