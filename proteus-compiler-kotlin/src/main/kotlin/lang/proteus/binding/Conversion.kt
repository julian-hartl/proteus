package lang.proteus.binding

import lang.proteus.symbols.TypeSymbol

internal sealed class Conversion(val exists: Boolean, val isIdentity: Boolean, val isImplicit: Boolean) {
    companion object {
        fun classify(from: TypeSymbol, to: TypeSymbol): Conversion {
            if (from == to) return IdentityConversion
            if(from.isAssignableTo(to)) return ImplicitConversion
            if (from == TypeSymbol.Boolean || from == TypeSymbol.Int) {
                if (to == TypeSymbol.String) return ExplicitConversion
            }
            if (from == TypeSymbol.String) {
                if (to == TypeSymbol.Boolean || to == TypeSymbol.Int) return ExplicitConversion
            }
            return NoConversion
        }
    }

    val isExplicit: Boolean
        get() = exists && !isImplicit
}

internal object ImplicitConversion : Conversion(
    exists = true,
    isIdentity = false,
    isImplicit = true
)

internal object ExplicitConversion : Conversion(
    exists = true,
    isIdentity = false,
    isImplicit = false
)

internal object IdentityConversion : Conversion(
    exists = true,
    isIdentity = true,
    isImplicit = true
)

internal object NoConversion : Conversion(
    exists = false,
    isIdentity = false,
    isImplicit = false
)