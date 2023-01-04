package lang.proteus.binding

import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf

sealed class ProteusType(val kType: KType) {
    object Int : ProteusType(kotlin.Int::class.createType())
    object Boolean : ProteusType(kotlin.Boolean::class.createType())

    object Object : ProteusType(Any::class.createType())

    object Type : ProteusType(KType::class.createType())

    object Identifier : ProteusType(Any::class.createType())

    override fun toString(): String {
        return this::class.simpleName!!
    }

    fun isAssignableTo(other: ProteusType): kotlin.Boolean {
        return other.kType.isSubtypeOf(kType)
    }

    companion object {

        fun fromName(name: String): ProteusType? {
            return when (name) {
                "Int" -> Int
                "Boolean" -> Boolean
                "Object" -> Object
                "Type" -> Type
                else -> null
            }
        }

        fun fromValue(value: Any): ProteusType {
            return when (value) {
                is kotlin.Int -> Int
                is kotlin.Boolean -> Boolean
                else -> Object
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
    }
}