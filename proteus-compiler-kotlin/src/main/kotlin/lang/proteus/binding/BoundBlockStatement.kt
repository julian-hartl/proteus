package lang.proteus.binding

import lang.proteus.symbols.TypeSymbol

internal class BoundBlockStatement(
    val statements: List<BoundStatement>,
) : BoundStatement() {
    override val type = statements.lastOrNull()?.type ?: TypeSymbol.Unit
}