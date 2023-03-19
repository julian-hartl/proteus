package lang.proteus.binding

import lang.proteus.diagnostics.Diagnostics
import lang.proteus.symbols.FunctionSymbol
import lang.proteus.symbols.GlobalVariableSymbol
import lang.proteus.symbols.StructMemberSymbol
import lang.proteus.symbols.StructSymbol

internal data class BoundProgram(
    val globalScope: BoundGlobalScope,
    val diagnostics: Diagnostics,
    val functionBodies: Map<FunctionSymbol, BoundBlockStatement>,
    val variableInitializers: Map<GlobalVariableSymbol, BoundExpression>,
    val structMembers: Map<StructSymbol, Set<StructMemberSymbol>>,
    val mainFunction: FunctionSymbol?,
)