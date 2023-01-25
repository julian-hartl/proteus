package lang.proteus.binding

import lang.proteus.diagnostics.Diagnostics
import lang.proteus.symbols.FunctionSymbol
import lang.proteus.symbols.GlobalVariableSymbol

internal data class BoundProgram(
    val globalScope: BoundGlobalScope,
    val diagnostics: Diagnostics,
    val functionBodies: Map<FunctionSymbol, BoundBlockStatement>,
    val variableInitializers: Map<GlobalVariableSymbol, BoundExpression>,
    val mainFunction: FunctionSymbol?,
)