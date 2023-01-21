package lang.proteus.symbols

sealed class VariableSymbol(override val name: String, val type: TypeSymbol, val isFinal: Boolean) : Symbol() {
    override fun toString(): String {
        return "$name: $type"
    }

    val isLocal: Boolean
        get() = this is LocalVariableSymbol

    val isParameter: Boolean
        get() = this is ParameterSymbol

    val isGlobal: Boolean
        get() = this is GlobalVariableSymbol
}

public class GlobalVariableSymbol(name: String, type: TypeSymbol, isFinal: Boolean) : VariableSymbol(
    name, type,
    isFinal
)

public open class LocalVariableSymbol(name: String, type: TypeSymbol, isFinal: Boolean) : VariableSymbol(
    name, type,
    isFinal
)

