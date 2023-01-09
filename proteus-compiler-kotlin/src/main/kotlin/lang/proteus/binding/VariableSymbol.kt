package lang.proteus.binding

data class VariableSymbol(val name: String, val type: ProteusType, val isFinal: Boolean ) {
    override fun toString(): String {
        return "$name: $type"
    }
}
