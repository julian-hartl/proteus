package lang.proteus.symbols

class ParameterSymbol(name: String, type: TypeSymbol) : LocalVariableSymbol(name, type, isFinal = true)