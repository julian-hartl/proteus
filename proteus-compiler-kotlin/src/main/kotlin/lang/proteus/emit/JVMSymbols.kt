package lang.proteus.emit

import lang.proteus.symbols.TypeSymbol

internal object JVMSymbols {

    @JvmStatic
    val valueTypeSymbols: Map<TypeSymbol, String> = mapOf(
        TypeSymbol.Int to "I",
        TypeSymbol.Boolean to "Z",
        TypeSymbol.Unit to "V"
    )

    @JvmStatic
    val primitiveTypeSymbols: Map<TypeSymbol, String> = mapOf(
        TypeSymbol.Int to "Ljava/lang/Integer",
        TypeSymbol.Boolean to "Ljava/lang/Boolean",
        TypeSymbol.String to "Ljava/lang/String",
    )

    fun isPointer(typeSymbol: TypeSymbol): Boolean {
        return !valueTypeSymbols.containsKey(typeSymbol)
    }


}