package lang.proteus.binding

import lang.proteus.symbols.TypeSymbol

internal class BoundMemberAccessExpression(
    val expression: BoundExpression,
    val memberName: String,
    override val type: TypeSymbol,
): BoundExpression()