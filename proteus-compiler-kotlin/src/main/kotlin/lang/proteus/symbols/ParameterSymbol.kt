package lang.proteus.symbols

internal class ParameterSymbol(name: String, type: TypeSymbol) : LocalVariableSymbol(name, type, isFinal = true)