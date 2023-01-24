package lang.proteus.symbols

import lang.proteus.syntax.parser.FunctionDeclarationSyntax

internal data class ProteusExternalFunction(val symbol: FunctionSymbol, val function: (List<Any?>) -> Any?) {
    fun call(arguments: List<Any?>): Any? {
        return function(arguments)
    }

    companion object {

        fun lookup(declaration: FunctionDeclarationSyntax): ProteusExternalFunction? {
            return try {
                val className = "lang.proteus.external.Functions"
                val clazz = Class.forName(className)
                val name = declaration.identifier.literal
                val method = clazz.methods.firstOrNull() { it.name == name } ?: return null
                val function = { args: List<Any?> ->
                    method.invoke(null, *args.toTypedArray())
                }
                val arguments: List<ParameterSymbol> =
                    method.parameterTypes.map { ParameterSymbol(it.name, TypeSymbol.fromJavaType(it)) }
                ProteusExternalFunction(
                    FunctionSymbol(name, arguments, TypeSymbol.fromJavaType(method.returnType), declaration),
                    function
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}

internal data class FunctionSymbol(
    override val name: String,
    val parameters: List<ParameterSymbol>,
    val returnType: TypeSymbol,
    val declaration: FunctionDeclarationSyntax,
) : Symbol()

