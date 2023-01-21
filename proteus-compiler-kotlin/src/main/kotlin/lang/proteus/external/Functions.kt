package lang.proteus.external

import java.lang.Error

class Functions {
    companion object {
        @JvmStatic
        fun println(s: Any) {
            kotlin.io.println(s)
        }

        @JvmStatic
        fun print(s: Any) {
            kotlin.io.print(s)
        }

        @JvmStatic
        fun input(): String {
            return readlnOrNull() ?: ""
        }

        @JvmStatic
        fun panic(s: String) {
            throw Error(s)
        }

        @JvmStatic
        fun multiply(a: Int, b: Int): Int {
            return a * b
        }

        @JvmStatic
        fun random(lower: Int, upper: Int): Int {
            return (Math.random() * (upper - lower)).toInt() + lower
        }
    }
}