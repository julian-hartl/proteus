package lang.proteus.emit

import lang.proteus.binding.BoundBinaryOperator
import lang.proteus.binding.BoundBinaryOperatorKind.*
import lang.proteus.binding.Conversion
import lang.proteus.symbols.TypeSymbol
import org.objectweb.asm.Opcodes

internal object JVMSymbols {

    @JvmStatic
    val valueTypeSymbols: Map<TypeSymbol, String> = mapOf(
        TypeSymbol.Int to "I",
        TypeSymbol.Boolean to "Z",
        TypeSymbol.Unit to "V"
    )

    @JvmStatic
    val primitiveTypeSymbols: Map<TypeSymbol, String> = mapOf(
        TypeSymbol.Int to "Ljava/lang/Integer",
        TypeSymbol.Boolean to "Ljava/lang/Boolean",
        TypeSymbol.String to "Ljava/lang/String",
    )

    fun getJVMPrimitiveType(type: TypeSymbol): String {
        return primitiveTypeSymbols[type] ?: throw IllegalArgumentException("Type $type is not a primitive type")
    }

    fun isPointer(typeSymbol: TypeSymbol): Boolean {
        return !valueTypeSymbols.containsKey(typeSymbol)
    }


    fun getJVMType(typeSymbol: TypeSymbol): String {
        return valueTypeSymbols[typeSymbol] ?: getJVMPrimitiveType(typeSymbol)
    }




    fun getBinaryOperatorOpCode(
        leftType: TypeSymbol,
        rightType: TypeSymbol,
        binaryOperator: BoundBinaryOperator,
    ): Int? {
        when (binaryOperator.kind) {
            Addition -> {
                if (leftType == TypeSymbol.Int && rightType == TypeSymbol.Int) {
                    return Opcodes.IADD
                }
            }

            Subtraction -> {
                if (leftType == TypeSymbol.Int && rightType == TypeSymbol.Int) {
                    return Opcodes.ISUB
                }
            }

            Multiplication -> TODO()
            Division -> TODO()
            Modulo -> TODO()
            Exponentiation -> TODO()
            LogicalAnd -> TODO()
            LogicalOr -> TODO()
            LogicalXor -> TODO()
            BitwiseAnd -> TODO()
            BitwiseOr -> TODO()
            BitwiseXor -> TODO()
            BitwiseShiftLeft -> TODO()
            BitwiseShiftRight -> TODO()
            Equality -> {
                if (leftType is TypeSymbol.Int && rightType is TypeSymbol.Int) {
                    return Opcodes.IF_ICMPEQ
                }
            }

            Inequality -> {
                if (leftType is TypeSymbol.Int && rightType is TypeSymbol.Int) {
                    return Opcodes.IF_ICMPNE
                }
            }

            LessThan -> {
                if (leftType is TypeSymbol.Int && rightType is TypeSymbol.Int) {
                    return Opcodes.IF_ICMPLT
                }
            }

            LessThanOrEqual -> {
                if (leftType is TypeSymbol.Int && rightType is TypeSymbol.Int) {
                    return Opcodes.IF_ICMPLE
                }
            }

            GreaterThan -> {
                if (leftType is TypeSymbol.Int && rightType is TypeSymbol.Int) {
                    return Opcodes.IF_ICMPGT
                }
            }

            GreaterThanOrEqual -> {
                if (leftType is TypeSymbol.Int && rightType is TypeSymbol.Int) {
                    return Opcodes.IF_ICMPGE
                }
            }

            TypeEquality -> TODO()
        }
        return null
    }

    fun emulateBinaryOperation(typeSymbol: TypeSymbol, binaryOperator: BoundBinaryOperator): JVMExternalFunction {
        return when (binaryOperator.kind) {
            Addition -> {
                when (typeSymbol) {
                    TypeSymbol.String -> JVMExternalFunctions.getJVMExternalFunction("concat")!!
                    else -> TODO()
                }
            }

            Subtraction -> TODO()
            Multiplication -> TODO()
            Division -> TODO()
            Modulo -> TODO()
            Exponentiation -> TODO()
            LogicalAnd -> TODO()
            LogicalOr -> TODO()
            LogicalXor -> TODO()
            BitwiseAnd -> TODO()
            BitwiseOr -> TODO()
            BitwiseXor -> TODO()
            BitwiseShiftLeft -> TODO()
            BitwiseShiftRight -> TODO()
            Equality -> TODO()
            Inequality -> TODO()
            LessThan -> TODO()
            LessThanOrEqual -> TODO()
            GreaterThan -> TODO()
            GreaterThanOrEqual -> TODO()
            TypeEquality -> TODO()
        }
    }

    fun emulateConversion(type: TypeSymbol, conversion: Conversion): JVMExternalFunction? {
        if (type is TypeSymbol.String) {
            return JVMExternalToStringFunction
        }
        return null
    }


}