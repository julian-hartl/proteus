package syntax.lexer

sealed class Keyword {

    companion object {

        fun fromString(value: String): Keyword? {
            return when (value) {
                "true" -> True
                "false" -> False
                else -> null
            }
        }

    }


    internal object True : Keyword()

    internal object False : Keyword()
}

