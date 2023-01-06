package lang.proteus.binding

import lang.proteus.binding.types.KotlinBinaryString
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

sealed class ProteusType(val kType: KType, val literal: kotlin.String) {
    object Int : ProteusType(kotlin.Int::class.createType(), "Int")
    object String : ProteusType(kotlin.String::class.createType(), "String")
    object Boolean : ProteusType(kotlin.Boolean::class.createType(), "Boolean")
    object Char : ProteusType(kotlin.Char::class.createType(), "Char")

    object BinaryString: ProteusType(KotlinBinaryString::class.createType(), "BinaryString")

    object Object : ProteusType(Any::class.createType(), "Object")

    object Type : ProteusType(KType::class.createType(), "Type")


    override fun toString(): kotlin.String {
        return literal
    }

    fun isAssignableTo(other: ProteusType): kotlin.Boolean {
        return other.kType.isSubtypeOf(kType)
    }

    companion object {

        fun fromName(name: kotlin.String): ProteusType? {
            return ProteusTypes.allTypes.find { it.literal == name }
        }

        fun fromValue(value: Any): ProteusType? {
            if(value is ProteusType){
                return ProteusType.Type
            }
            return fromKotlinType(value::class.createType())
        }


        fun fromKotlinTypeOrObject(kType: KType): ProteusType {
            return fromKotlinType(kType) ?: Object
        }

        fun fromKotlinType(kType: KType): ProteusType? {
            return ProteusTypes.allTypes.find { it.kType == kType }
        }

        fun fromValueOrObject(value: Any): ProteusType {
            return fromValue(value) ?: Object
        }
    }
}