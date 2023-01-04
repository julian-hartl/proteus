package binding

import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf

sealed class BoundType(val kType: KType) {
    object Int : BoundType(kotlin.Int::class.createType())
    object Boolean : BoundType(kotlin.Boolean::class.createType())

    object Object : BoundType(kotlin.Any::class.createType())

    override fun toString(): String {
        return this::class.simpleName!!
    }

    fun isAssignableTo(other: BoundType): kotlin.Boolean {
        return other.kType.isSubtypeOf(kType)
    }

    companion object {
        fun fromKotlinTypeOrObject(kType: KType): BoundType {
            return fromKotlinType(kType) ?: Object
        }

        fun fromKotlinType(kType: KType): BoundType? {
            return when {
                kType.isSubtypeOf(Int.kType) -> Int
                kType.isSubtypeOf(Boolean.kType) -> Boolean
                kType.isSubtypeOf(Object.kType) -> Object
                else -> null
            }
        }
    }
}