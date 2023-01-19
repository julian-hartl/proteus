package lang.proteus.external

import java.lang.Error

class Functions {
    companion object {
        @JvmStatic
        fun println(s: String) {
            println(s)
        }

        @JvmStatic
        fun print(s: String) {
            print(s)
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
    }
}