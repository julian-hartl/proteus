package lang.proteus.symbols


internal class ParameterSymbol(name: String, type: TypeSymbol, moduleReferenceSymbol: ModuleReferenceSymbol) :
    LocalVariableSymbol(name, type, type, isFinal = true, isConst = false, moduleReferenceSymbol)