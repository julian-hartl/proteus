package lang.proteus.syntax.lexer

import lang.proteus.diagnostics.DiagnosticsBag
import lang.proteus.syntax.lexer.token_lexers.*
import lang.proteus.text.SourceText

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
    private val sourceText: SourceText,
    private var position: Int,
    val diagnosticsBag: DiagnosticsBag,
) {


    constructor(sourceText: SourceText) : this(sourceText, 0, DiagnosticsBag())


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
            val tokenLexerResult = matchingLexer.submit(start, position, literal.toString())
            if (tokenLexerResult != null) {
                val token = tokenLexerResult.syntaxToken
                val difference = length - tokenLexerResult.consumed
                position -= difference
                return token
            }
        }

        diagnosticsBag.reportBadCharacter(current, position)
        next()
        return SyntaxToken.badToken(position, current.toString())
    }


    private fun next() {
        position++
    }

    private fun peek(offset: Int): Char {
        val index = position + offset
        if (index >= sourceText.length) {
            return '\u0000'
        }
        return sourceText[index]
    }

    private val current: Char
        get() = peek(0)


}