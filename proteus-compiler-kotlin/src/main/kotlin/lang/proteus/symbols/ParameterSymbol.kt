package lang.proteus.symbols

class ParameterSymbol(name: String, type: TypeSymbol) : VariableSymbol(name, type, isFinal = true)