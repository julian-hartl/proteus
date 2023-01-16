package lang.proteus.symbols

import lang.proteus.binding.ProteusType

data class VariableSymbol(override val name: String, val type: ProteusType, val isFinal: Boolean) : Symbol() {
    override fun toString(): String {
        return "$name: $type"
    }
}

