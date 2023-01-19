package lang.proteus.symbols

open class VariableSymbol(override val name: String, val type: TypeSymbol, val isFinal: Boolean) : Symbol() {
    override fun toString(): String {
        return "$name: $type"
    }
}

