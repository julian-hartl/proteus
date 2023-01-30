package lang.proteus.symbols

sealed class Symbol(uniqueIdentifier: String, val simpleName: String) {
    val qualifiedName: String

    init {
        qualifiedName = "${simpleName}_${uniqueIdentifier}"
    }

    override fun toString(): String = simpleName

    fun conflictsWith(other: Symbol): Boolean {
        return simpleName == other.simpleName && other::class == this::class
    }
}