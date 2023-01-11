package lang.proteus.syntax.lexer.token

import kotlin.reflect.KClass

internal object Tokens {
    val allTokens: List<Token>
        get() = Token::class.sealedSubclasses
            .map {
                getSealedSubclasses(it)
            }.flatten()

    private fun getSealedSubclasses(clazz: KClass<*>): List<Token> {
        if (clazz.objectInstance != null) return listOf(clazz.objectInstance as Token)
        return clazz.sealedSubclasses
            .map {
                getSealedSubclasses(it)
            }.flatten()
    }

    fun fromLiteral(currentLiteral: String): Token? {
        return allTokens.firstOrNull { it.literal == currentLiteral }
    }

}