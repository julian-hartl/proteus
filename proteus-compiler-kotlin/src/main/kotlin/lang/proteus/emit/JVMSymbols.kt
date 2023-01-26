package lang.proteus.emit

import lang.proteus.binding.BoundBinaryOperator
import lang.proteus.binding.BoundBinaryOperatorKind.*
import lang.proteus.binding.BoundExpression
import lang.proteus.symbols.TypeSymbol
import org.objectweb.asm.Opcodes

internal object JVMSymbols {

    @JvmStatic
    val typeSymbols: Map<TypeSymbol, String> = mapOf(
        TypeSymbol.Int to "I",
        TypeSymbol.Boolean to "Z",
        TypeSymbol.String to "Ljava/lang/String;",
        TypeSymbol.Type to "Ljava/lang/Class;",
        TypeSymbol.Any to "Ljava/lang/Object;",
        TypeSymbol.Error to "Ljava/lang/Object;",
        TypeSymbol.Unit to "V"
    )

    fun getJVMType(typeSymbol: TypeSymbol): String {
        return typeSymbols[typeSymbol] ?: "Ljava/lang/Object;"
    }

    fun getStoreOpCode(typeSymbol: TypeSymbol): Int {
        return when (typeSymbol) {
            TypeSymbol.Int -> Opcodes.ISTORE
            TypeSymbol.Boolean -> Opcodes.ISTORE
            TypeSymbol.String -> Opcodes.ASTORE
            TypeSymbol.Type -> Opcodes.ASTORE
            TypeSymbol.Any -> Opcodes.ASTORE
            TypeSymbol.Error -> Opcodes.ASTORE
            TypeSymbol.Unit -> Opcodes.ASTORE
        }
    }

    fun getLoadOpCode(typeSymbol: TypeSymbol): Int {
        return when (typeSymbol) {
            TypeSymbol.Int -> Opcodes.ILOAD
            TypeSymbol.Boolean -> Opcodes.ILOAD
            TypeSymbol.String -> Opcodes.ALOAD
            TypeSymbol.Type -> Opcodes.ALOAD
            TypeSymbol.Any -> Opcodes.ALOAD
            TypeSymbol.Error -> Opcodes.ALOAD
            TypeSymbol.Unit -> Opcodes.ALOAD
        }
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
            Equality -> {
                if(leftType is TypeSymbol.Int && rightType is TypeSymbol.Int) {
                    return Opcodes.IF_ICMPEQ
                }
            }

            Inequality ->{
                if(leftType is TypeSymbol.Int && rightType is TypeSymbol.Int) {
                    return Opcodes.IF_ICMPNE
                }
            }
            LessThan -> {
                if(leftType is TypeSymbol.Int && rightType is TypeSymbol.Int) {
                    return Opcodes.IF_ICMPLT
                }
            }

            LessThanOrEqual -> {
                if(leftType is TypeSymbol.Int && rightType is TypeSymbol.Int) {
                    return Opcodes.IF_ICMPLE
                }
            }
            GreaterThan -> {
                if(leftType is TypeSymbol.Int && rightType is TypeSymbol.Int) {
                    return Opcodes.IF_ICMPGT
                }
            }
            GreaterThanOrEqual -> {
                if(leftType is TypeSymbol.Int && rightType is TypeSymbol.Int) {
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


}