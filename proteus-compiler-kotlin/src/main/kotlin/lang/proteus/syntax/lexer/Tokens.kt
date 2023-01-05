package lang.proteus.syntax.lexer

import kotlin.reflect.KClass

object Tokens {
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
}