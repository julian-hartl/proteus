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
        return when (this) {
            is Int -> symbol is Int
            is Boolean -> symbol is Boolean
            is String -> symbol is String
            is Type -> symbol is Type
            is Error -> symbol is Error
            is Any -> true
        }
    }


}