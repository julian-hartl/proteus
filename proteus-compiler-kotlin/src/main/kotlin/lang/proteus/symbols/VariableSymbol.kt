package lang.proteus.symbols

import lang.proteus.symbols.TypeSymbol

data class VariableSymbol(override val name: String, val type: TypeSymbol, val isFinal: Boolean) : Symbol() {
    override fun toString(): String {
        return "$name: $type"
    }
}

