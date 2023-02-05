package lang.proteus.symbols

 sealed class Symbol(val simpleName: String, val moduleReferenceSymbol: ModuleReferenceSymbol) {
    val qualifiedName: String

    init {
        qualifiedName = "$moduleReferenceSymbol.$simpleName"
    }

    override fun toString(): String = simpleName

    fun conflictsWith(other: Symbol): Boolean {
        return simpleName == other.simpleName && other::class == this::class
    }
}