package lang.proteus.symbols

sealed class TypeSymbol(name: kotlin.String) :
    Symbol("type", name) {


    object Unit : TypeSymbol("Unit")
    object Int : TypeSymbol("Int")

    object Boolean : TypeSymbol("Boolean")

    object String : TypeSymbol("String")
    object Type : TypeSymbol("Type")

    object Any : TypeSymbol("Any")

    object Error : TypeSymbol("?")

    data class Struct(val name: kotlin.String) : TypeSymbol(name) {
        override fun toString(): kotlin.String {
            return name
        }
    }

    data class Pointer(val type: TypeSymbol, val isMutable: kotlin.Boolean) : TypeSymbol(type.simpleName) {
        override fun toString(): kotlin.String {
            return "&${if (isMutable) "mut " else ""}$type"
        }
    }

    companion object {

        private val internalTypes = lazy {
            TypeSymbol::class.sealedSubclasses.filter {
                try {
                    it.objectInstance != null
                } catch (e: Exception) {
                    println("Warning: Could not load type ${it.simpleName}")
                    false
                }
            }.map { it.objectInstance!! }
        }

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
            return internalTypes.value.firstOrNull { it.simpleName == name }
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
            is Pointer -> {
                return this is Pointer && this.type.isAssignableTo(symbol.type) && this.isMutable == symbol.isMutable
            }
        }
    }

    override fun equals(other: kotlin.Any?): kotlin.Boolean {
        return when (other) {
            is TypeSymbol -> this.qualifiedName == other.qualifiedName && this.isPointer() == other.isPointer()
            else -> false
        }
    }

    fun ref(asMut: kotlin.Boolean): TypeSymbol {
        return Pointer(this, asMut)
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