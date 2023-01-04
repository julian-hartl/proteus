package binding

import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf

sealed class BoundType(val kType: KType) {
    object Int : BoundType(kotlin.Int::class.createType())
    object Boolean : BoundType(kotlin.Boolean::class.createType())

    object Object : BoundType(Any::class.createType())

    object Type : BoundType(KType::class.createType())

    override fun toString(): String {
        return this::class.simpleName!!
    }

    fun isAssignableTo(other: BoundType): kotlin.Boolean {
        return other.kType.isSubtypeOf(kType)
    }

    companion object {

        fun fromName(name: String): BoundType? {
            return when (name) {
                "Int" -> Int
                "Boolean" -> Boolean
                "Object" -> Object
                "Type" -> Type
                else -> null
            }
        }

        fun fromValue(value: Any): BoundType {
            return when (value) {
                is kotlin.Int -> Int
                is kotlin.Boolean -> Boolean
                else -> Object
            }
        }


        fun fromKotlinTypeOrObject(kType: KType): BoundType {
            return fromKotlinType(kType) ?: Object
        }

        fun fromKotlinType(kType: KType): BoundType? {
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