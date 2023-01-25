package lang.proteus.symbols

import lang.proteus.syntax.parser.FunctionDeclarationSyntax
import lang.proteus.syntax.parser.SyntaxTree

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
                val functionSymbol = FunctionSymbol(
                    null,
                    TypeSymbol.fromJavaType(method.returnType),
                    declaration,
                    declaration.syntaxTree
                )
                val arguments: List<ParameterSymbol> =
                    method.parameterTypes.map {
                        ParameterSymbol(
                            it.name,
                            TypeSymbol.fromJavaType(it),
                            declaration.syntaxTree,
                            functionSymbol
                        )
                    }
                functionSymbol.parameters = arguments
                ProteusExternalFunction(
                    functionSymbol,
                    function
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}

internal class FunctionSymbol(
    parameters: List<ParameterSymbol>?,
    val returnType: TypeSymbol,
    val declaration: FunctionDeclarationSyntax,
    syntaxTree: SyntaxTree,
) : Symbol(syntaxTree.id.toString(), declaration.identifier.literal) {
    var parameters: List<ParameterSymbol>

    init {
        this.parameters = parameters ?: emptyList()
    }
}

