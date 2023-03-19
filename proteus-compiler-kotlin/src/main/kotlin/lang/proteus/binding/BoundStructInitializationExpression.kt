package lang.proteus.binding

import lang.proteus.symbols.StructSymbol
import lang.proteus.symbols.TypeSymbol

internal class BoundStructInitializationExpression(
    val struct: StructSymbol,
    val members: List<BoundStructMemberInitializationExpression>,
): BoundExpression() {
    override val type: TypeSymbol
        get() = TypeSymbol.Struct(struct.name)
}

internal class BoundStructMemberInitializationExpression(
    val name: String,
    val expression: BoundExpression,
)