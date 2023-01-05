package lang.proteus.binding

import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf

object ProteusTypes {
    val allTypes: List<ProteusType>
        get() = ProteusType::class.sealedSubclasses
            .map {
                it.objectInstance!!
            }
}

sealed class ProteusType(val kType: KType, val literal: String) {
    object Int : ProteusType(kotlin.Int::class.createType(), "Int")
    object Boolean : ProteusType(kotlin.Boolean::class.createType(), "Boolean")

    object Object : ProteusType(Any::class.createType(), "Object")

    object Type : ProteusType(KType::class.createType(), "Type")


    override fun toString(): String {
        return literal
    }

    fun isAssignableTo(other: ProteusType): kotlin.Boolean {
        return other.kType.isSubtypeOf(kType)
    }

    companion object {

        fun fromName(name: String): ProteusType? {
            return ProteusTypes.allTypes.find { it.literal == name }
        }

        fun fromValue(value: Any): ProteusType? {
            if (value is ProteusType) {
                return Type
            }
            return when (value) {
                is kotlin.Int -> Int
                is kotlin.Boolean -> Boolean
                else -> null
            }
        }


        fun fromKotlinTypeOrObject(kType: KType): ProteusType {
            return fromKotlinType(kType) ?: Object
        }

        fun fromKotlinType(kType: KType): ProteusType? {
            return when {
                kType.isSubtypeOf(Int.kType) -> Int
                kType.isSubtypeOf(Boolean.kType) -> Boolean
                kType.isSubtypeOf(Object.kType) -> Object
                kType.isSubtypeOf(Type.kType) -> Type
                else -> null
            }
        }

        fun fromValueOrObject(value: Any): ProteusType {
            return fromValue(value) ?: Object
        }
    }
}