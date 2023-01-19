package lang.proteus.symbols

internal data class ProteusExternalFunction(val symbol: FunctionSymbol, val function: (List<Any?>) -> Any?) {
    fun call(arguments: List<Any?>): Any? {
        return function(arguments)
    }

    companion object {

        private fun lookup(name: String, arguments: List<ParameterSymbol>): ProteusExternalFunction? {
            return try {
                val className = "lang.proteus.external.Functions"
                val clazz = Class.forName(className)
                val method = clazz.getMethod(name, *arguments.map { it.type.getJavaClass() }.toTypedArray())
                val function = { args: List<Any?> ->
                    method.invoke(null, *args.toTypedArray())
                }
                ProteusExternalFunction(FunctionSymbol(name, arguments, TypeSymbol.fromJavaType(method.returnType)), function)
            } catch (e: Exception) {
                null
            }
        }
    }
}

internal object BuiltInFunctions {
    public val Print = FunctionSymbol("print", listOf(ParameterSymbol("value", TypeSymbol.String)), TypeSymbol.Unit)
    public val Input = FunctionSymbol("input", emptyList(), TypeSymbol.String)

    val allFunctions = listOf(Print, Input)

    fun fromName(name: String): FunctionSymbol? {
        return allFunctions.firstOrNull { it.name == name }
    }

}

data class FunctionSymbol(
    override val name: String,
    val parameters: List<ParameterSymbol>,
    val returnType: TypeSymbol,
) : Symbol()

