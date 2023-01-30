package lang.proteus.ffi

import lang.proteus.binding.BoundExpression
import lang.proteus.emit.Emitter

internal abstract class ProteusFFI<E : Emitter<*>>(val emitter: E) {

    fun call(name: String, arguments: List<BoundExpression>) {
        when (name) {
            "println" -> println(arguments)
            else -> throw IllegalArgumentException("Unknown FFI function $name")
        }
    }

    abstract fun println(arguments: List<BoundExpression>)

}