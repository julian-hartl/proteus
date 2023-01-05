package lang.proteus.syntax.lexer

import lang.proteus.diagnostics.DiagnosticsBag
import lang.proteus.syntax.lexer.token_lexers.*

internal object Lexers {
    val allLexers: List<TokenLexer>
        get() = listOf(
            WhiteSpaceTokenLexer,
            NumberTokenLexer,
            LetterTokenLexer,
            NonLetterTokenLexer,
            EndOfFileTokenLexer
        )
}

internal class Lexer private constructor(
    private val input: String,
    private var position: Int,
    val diagnosticsBag: DiagnosticsBag
) {


    constructor(input: String) : this(input, 0, DiagnosticsBag())


    private val tokenLexers: List<TokenLexer> = Lexers.allLexers

    fun nextToken(): SyntaxToken<*> {

        val matchingLexer = tokenLexers.firstOrNull { it.match(current) }
        if (matchingLexer != null) {
            var length = 0
            val start = position
            val literal = StringBuilder()
            while (matchingLexer.match(current) && length < (matchingLexer.maxLength ?: Int.MAX_VALUE)) {
                length++
                literal.append(current)
                next()
            }
            val token = matchingLexer.submit(start, position, literal.toString())
            if (token != null) {
                val span = token.span()
                val spanLength = span.end - span.start
                val difference = length - spanLength
                position -= difference
                return token
            }
        }

        diagnosticsBag.reportUnexpectedCharacter(current, position)
        next()
        return SyntaxToken.badToken(position, current.toString())


    }


    private fun next() {
        position++
    }

    private fun peek(offset: Int): Char {
        val index = position + offset
        if (index >= input.length) {
            return '\u0000'
        }
        return input[index]
    }

    private val current: Char
        get() = peek(0)


}