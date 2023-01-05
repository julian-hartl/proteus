package lang.proteus.syntax.lexer

import lang.proteus.binding.ProteusType
import lang.proteus.diagnostics.DiagnosticsBag

internal object Lexers {
    val allLexers: List<TokenLexer>
        get() = TokenLexer::class.sealedSubclasses
            .map {
                it.objectInstance!!
            }
}

internal sealed class TokenLexer {
    abstract fun match(current: Char): Boolean

    abstract fun submit(start: Int, position: Int, literal: String): SyntaxToken<*>?
}

internal object NumberTokenLexer : TokenLexer() {
    override fun match(current: Char): Boolean {
        return current.isDigit()
    }

    override fun submit(start: Int, position: Int, literal: String): SyntaxToken<*> {
        return SyntaxToken.numberToken(start, literal)
    }

}

internal object LetterTokenLexer : TokenLexer() {
    override fun match(current: Char): Boolean {
        return current.isLetter()
    }

    override fun submit(start: Int, position: Int, literal: String): SyntaxToken<*> {
        val type = ProteusType.fromName(literal)
        if (type != null) {
            return SyntaxToken.typeToken(start, literal, type)
        }
        val operator = Operators.fromLiteral(literal)
        if (operator != null) {
            return operator.toSyntaxToken(start)
        }
        return SyntaxToken.keywordToken(start, literal)
    }

}

internal object NonLetterTokenLexer : TokenLexer() {
    override fun match(current: Char): Boolean {
        return !current.isLetter()
    }

    override fun submit(start: Int, position: Int, literal: String): SyntaxToken<*>? {
        val operator = Operators.fromLiteral(literal)
        if (operator != null) {
            return operator.toSyntaxToken(start)
        }
        return null
    }

}

internal object WhiteSpaceTokenLexer : TokenLexer() {
    override fun match(current: Char): Boolean {
        return current.isWhitespace()
    }

    override fun submit(start: Int, position: Int, literal: String): SyntaxToken<*> {
        return SyntaxToken.whiteSpaceToken(start, literal)
    }

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
            val start = position
            while (matchingLexer.match(current)) {
                next()
            }
            val literal = input.substring(start, position)
            val token = matchingLexer.submit(start, position, literal)
            if (token != null) {
                return token
            }
        }
        if (current == '\u0000') {
            return SyntaxToken.endOfFile(position)
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