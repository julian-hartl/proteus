package lang.proteus.binding

import lang.proteus.symbols.TypeSymbol

internal sealed class Conversion() {
    companion object {
        fun classify(from: TypeSymbol, to: TypeSymbol): Conversion {
            if (from == to) return IdentityConversion
            if (from.isAssignableTo(to)) return ImplicitConversion
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
        get() = this is ExplicitConversion

    val isImplicit: Boolean
        get() = this is ImplicitConversion

    val isIdentity: Boolean
        get() = this is IdentityConversion

    val isNone: Boolean
        get() = this is NoConversion

    val exists get() = !isNone
}

internal object ImplicitConversion : Conversion()

internal object ExplicitConversion : Conversion()

internal object IdentityConversion : Conversion()

internal object NoConversion : Conversion()