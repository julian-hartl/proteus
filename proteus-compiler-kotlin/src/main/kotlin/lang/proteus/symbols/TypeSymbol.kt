package lang.proteus.symbols

import lang.proteus.emit.MemoryLayout

sealed class TypeSymbol(name: kotlin.String) :
    Symbol("type", name) {

    object Int : TypeSymbol("Int")

    object Boolean : TypeSymbol("Boolean")

    object String : TypeSymbol("String")
    object Type : TypeSymbol("Type")

    object Any : TypeSymbol("Any")

    object Error : TypeSymbol("?")
    object Unit : TypeSymbol("Unit")

    data class Struct(val name: kotlin.String) : TypeSymbol(name) {
        override fun toString(): kotlin.String {
            return name
        }
    }

    data class Pointer(val type: TypeSymbol) : TypeSymbol(type.simpleName) {
        override fun toString(): kotlin.String {
            return "$type*"
        }
    }

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

        fun fromName(name: kotlin.String): TypeSymbol {
            return internalTypes.firstOrNull { it.simpleName == name } ?: Struct(name)
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
            is Struct -> this is Struct && this.qualifiedName == symbol.qualifiedName
            is Pointer -> this is Pointer && this.type.isAssignableTo(symbol.type)
        }
    }

    override fun equals(other: kotlin.Any?): kotlin.Boolean {
        return when (other) {
            is TypeSymbol -> this.qualifiedName == other.qualifiedName && this.isPointer() == other.isPointer()
            else -> false
        }
    }

    fun ref(): TypeSymbol {
        return Pointer(this)
    }

    fun deref(): TypeSymbol {
        if(this is Pointer) {
            return this.type
        }
        return this
    }

    fun isPointer(): kotlin.Boolean {
        return this is Pointer
    }

    override fun toString(): kotlin.String {
        return when (this) {
            is Pointer -> "$type*"
            else -> simpleName
        }
    }

    override fun hashCode(): kotlin.Int {
        return javaClass.hashCode()
    }


}