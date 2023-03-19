package lang.proteus.binding

import lang.proteus.symbols.TypeSymbol

object TypeConverter {
    fun convert(value: Any, type: TypeSymbol): Any {
        val deref = type.deref()
        if(deref is TypeSymbol.Struct) {
            return value
        }
        return when (deref) {
            TypeSymbol.Int -> Integer.parseInt(value.toString())
            TypeSymbol.Boolean -> parseBoolean(value.toString())
            TypeSymbol.String -> value.toString()
            else -> throw IllegalStateException("Cannot convert $value to $type")
        }
    }

    private fun parseBoolean(toString: String): Any {
        return when (toString) {
            "true" -> true
            "false" -> false
            else -> throw IllegalStateException("Cannot parse $toString to boolean")
        }
    }
}