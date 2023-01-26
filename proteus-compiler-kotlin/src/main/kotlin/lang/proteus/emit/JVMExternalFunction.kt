package lang.proteus.emit

import lang.proteus.binding.BoundExpression
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

    fun generateCall(mv: MethodVisitor, emitter: Emitter, arguments: List<BoundExpression>)

}

internal class JVMStringConcatFunction : JVMExternalFunction {

    override fun generateCall(mv: MethodVisitor, emitter: Emitter, arguments: List<BoundExpression>) {
        for(argument in arguments) {
            emitter.generateExpression(mv, argument)
        }
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "concat", "(Ljava/lang/String;)Ljava/lang/String;", false)
    }

}

internal class JVMExternalPrintlnFunction : JVMExternalFunction {

    override fun generateCall(mv: MethodVisitor, emitter: Emitter, arguments: List<BoundExpression>) {
        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
        for(argument in arguments) {
            emitter.generateExpression(mv, argument)
        }
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false)
    }

}