package lang.proteus.symbols

sealed class Symbol {
    abstract val name: String

    override fun toString(): String = name
}