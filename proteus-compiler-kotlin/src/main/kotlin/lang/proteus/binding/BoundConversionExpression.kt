package lang.proteus.binding

import lang.proteus.symbols.TypeSymbol

internal data class BoundConversionExpression(override val type: TypeSymbol, val expression: BoundExpression, val conversion: Conversion) :
    BoundExpression() {
}