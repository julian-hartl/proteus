package lang.proteus.symbols

sealed class TypeSymbol(name: kotlin.String, id: kotlin.String? = null) : Symbol(id ?: "proteus-built-in", name) {

    object Int : TypeSymbol("Int")

    object Boolean : TypeSymbol("Boolean")

    object String : TypeSymbol("String")
    object Type : TypeSymbol("Type")

    object Any : TypeSymbol("Any")

    object Error : TypeSymbol("?")
    object Unit : TypeSymbol("Unit")

    class Struct(val name: kotlin.String) : TypeSymbol(name, "struct")

    companion object {

        val internalTypes = TypeSymbol::class.sealedSubclasses.filter {
            it.objectInstance != null 
        }.map { it.objectInstance!! }

        fun fromValueOrAny(value: kotlin.Any?): TypeSymbol {
            return when (value) {
                is kotlin.Int -> Int
                is kotlin.Boolean -> Boolean
                is kotlin.String -> String
                is TypeSymbol -> Type
                else -> Any
            }
        }

        fun fromName(name: kotlin.String): TypeSymbol? {
            return internalTypes.firstOrNull { it.simpleName == name }
        }

        fun fromJavaType(javaType: java.lang.reflect.Type): TypeSymbol {
            return when (javaType) {
                java.lang.Integer.TYPE -> Int
                java.lang.Boolean.TYPE -> Boolean
                java.lang.String::class.java -> String
                java.lang.Void.TYPE -> Unit
                else -> Any
            }
        }
    }


    fun isAssignableTo(symbol: TypeSymbol): kotlin.Boolean {
        return when (symbol) {
            Int -> this is Int
            Boolean -> this is Boolean
            String -> this is String
            Type -> this is Type
            Error -> this is Error
            Any -> true
            Unit -> this is Unit
            is Struct -> this is Struct && this.name == symbol.name
        }
    }


}