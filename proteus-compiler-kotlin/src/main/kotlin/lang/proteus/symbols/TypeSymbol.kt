package lang.proteus.symbols

sealed class TypeSymbol(override val name: kotlin.String) : Symbol() {

    object Int : TypeSymbol("Int")

    object Boolean : TypeSymbol("Boolean")

    object String : TypeSymbol("String")
    object Type : TypeSymbol("Type")

    object Any : TypeSymbol("Any")

    object Error : TypeSymbol("?")

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
    }

    fun isAssignableTo(symbol: TypeSymbol): kotlin.Boolean {
        return when (symbol) {
            is Int -> this is Int
            is Boolean -> this is Boolean
            is String -> this is String
            is Type -> this is Type
            is Error -> this is Error
            is Any -> true
        }
    }


}