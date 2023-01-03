package lexer

import diagnostics.Diagnostics

class Lexer private constructor(private val input: String, private var position: Int, val diagnostics: Diagnostics) {


    constructor(input: String) : this(input, 0, Diagnostics())


    fun nextToken(): SyntaxToken<*> {
        if (position >= input.length) {
            return SyntaxToken(SyntaxKind.EndOfFileToken, position, "", null)
        }

        if (current.isDigit()) {
            val start = position
            while (current.isDigit()) {
                next()
            }
            val literal = input.substring(start, position)
            return SyntaxToken.numberToken(start, literal)
        }

        if (current.isWhitespace()) {
            val start = position
            while (current.isWhitespace()) {
                next()
            }
            val literal = input.substring(start, position)
            return SyntaxToken.whiteSpaceToken(start, literal)
        }

        if (isCurrentOperator()) {
            val start = position
            val operator = current.toString()
            next()
            return SyntaxToken.fromOperator(start, operator)
        }

        if (current == '=') {
            val start = position
            if (peek(1) == '=' && peek(2).isWhitespace()) {
                next()
                return SyntaxToken.equalityToken(start)
            }
            if (peek(-1) == '=') {
                next()
                return nextToken()
            }
        }

        diagnostics.add("Unexpected character", current.toString(), position)
        next()
        return SyntaxToken.badToken(position, current.toString())

    }

    private fun isCurrentOperator(): Boolean {
        return Operator.isOperator(current.toString())
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