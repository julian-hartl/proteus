package lang.proteus.binding

data class VariableSymbol(val name: String, val type: ProteusType) {
    override fun toString(): String {
        return "$name: $type"
    }
}
