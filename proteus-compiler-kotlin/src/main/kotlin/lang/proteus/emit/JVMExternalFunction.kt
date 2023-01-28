package lang.proteus.emit

import lang.proteus.binding.BoundExpression
import lang.proteus.symbols.TypeSymbol
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes


internal object JVMExternalFunctions {

    @JvmStatic
    val functions: Map<String, JVMExternalFunction> = mapOf(
        "println" to JVMExternalPrintlnFunction(),
        "concat" to JVMStringConcatFunction(),
    )

    fun getJVMExternalFunction(name: String): JVMExternalFunction? {
        return functions[name]
    }

}

internal interface JVMExternalFunction {

    fun generateCall(mv: MethodVisitor, emitter: JVMEmitter, arguments: List<BoundExpression>)

}

internal class JVMStringConcatFunction : JVMExternalFunction {

    override fun generateCall(mv: MethodVisitor, emitter: JVMEmitter, arguments: List<BoundExpression>) {
        for (argument in arguments) {
            emitter.generateExpression(argument)
        }
        mv.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "java/lang/String",
            "concat",
            "(Ljava/lang/String;)Ljava/lang/String;",
            false
        )
    }

}

internal class JVMExternalPrintlnFunction : JVMExternalFunction {

    override fun generateCall(mv: MethodVisitor, emitter: JVMEmitter, arguments: List<BoundExpression>) {
        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
        for (argument in arguments) {
            emitter.generateExpression(argument)
        }
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false)
    }

}

internal object JVMExternalToStringFunction : JVMExternalFunction {

    override fun generateCall(mv: MethodVisitor, emitter: JVMEmitter, arguments: List<BoundExpression>) {
        val argument = arguments[0]
        emitter.generateExpression(argument)
        if (argument.type is TypeSymbol.String) {
            return
        }
        val argumentDescriptor = ""
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            "java/lang/String",
            "valueOf",
            "($argumentDescriptor)Ljava/lang/String;",
            false
        )
    }

}

internal object JVMExternalToPrimitiveTypeFunction : JVMExternalFunction {

    override fun generateCall(mv: MethodVisitor, emitter: JVMEmitter, arguments: List<BoundExpression>) {
        val argument = arguments[0]
        emitter.generateExpression(argument)
        val primitiveType = JVMSymbols.getJVMPrimitiveType(argument.type)
        val argumentDescriptor = ""
        val primitiveTypeDescriptor = ""
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            primitiveType.substring(1),
            "valueOf",
            "($argumentDescriptor)${primitiveTypeDescriptor}",
            false
        )
    }

}