package lang.proteus.symbols

import lang.proteus.external.Functions

internal data class ProteusExternalFunction(val symbol: FunctionSymbol, val function: (List<Any?>) -> Any?) {
    fun call(arguments: List<Any?>): Any? {
        return function(arguments)
    }

    companion object {

        fun lookup(name: String): ProteusExternalFunction? {
            return try {
                val className = "lang.proteus.external.Functions"
                val clazz = Class.forName(className)
                val method = clazz.methods.firstOrNull() { it.name == name } ?: return null
                val function = { args: List<Any?> ->
                    method.invoke(null, *args.toTypedArray())
                }
                val arguments: List<ParameterSymbol> =
                    method.parameterTypes.map { ParameterSymbol(it.name, TypeSymbol.fromJavaType(it)) }
                ProteusExternalFunction(
                    FunctionSymbol(name, arguments, TypeSymbol.fromJavaType(method.returnType)),
                    function
                )
            } catch (e: Exception) {
                e.printStackTrace()
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

