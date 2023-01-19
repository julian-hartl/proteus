package lang.proteus.symbols

sealed class TypeSymbol(override val name: kotlin.String) : Symbol() {

    object Int : TypeSymbol("Int")

    object Boolean : TypeSymbol("Boolean")

    object String : TypeSymbol("String")
    object Type : TypeSymbol("Type")

    object Any : TypeSymbol("Any")

    object Error : TypeSymbol("?")
    object Unit : TypeSymbol("Unit")

    companion object {

        val allTypes = TypeSymbol::class.sealedSubclasses.map { it.objectInstance!! }

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
            return allTypes.firstOrNull { it.name == name }
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

    fun getJavaClass(): Class<*> {
        return when (this) {
            Int -> java.lang.Integer.TYPE
            Boolean -> java.lang.Boolean.TYPE
            String -> java.lang.String::class.java
            Type -> TypeSymbol::class.java
            Any -> java.lang.Object::class.java
            Error -> java.lang.Object::class.java
            Unit -> java.lang.Void.TYPE
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
        }
    }


}