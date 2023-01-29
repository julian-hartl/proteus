package lang.proteus.ffi

import lang.proteus.binding.BoundExpression
import lang.proteus.emit.JVMEmitter

internal class JvmFFI(emitter: JVMEmitter) : ProteusFFI<JVMEmitter>(emitter) {
    override fun println(arguments: List<BoundExpression>) {
        emitter.jvmClass.visitNativeField("java/lang/System", "out", "Ljava/io/PrintStream;")
        val argument = arguments[0]
        emitter.generateExpression(argument)
        emitter.jvmClass.callNativeMethod("java/io/PrintStream", "println", "(Ljava/lang/String;)V")
    }

}