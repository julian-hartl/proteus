package syntax.lexer

import diagnostics.Diagnostics

class Lexer private constructor(private val input: String, private var position: Int, val diagnostics: Diagnostics) {


    constructor(input: String) : this(input, 0, Diagnostics())


    fun nextToken(): SyntaxToken<*> {
        if (position >= input.length) {
            return SyntaxToken(Token.EndOfFile, position, "", null)
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
        // check if it's an operator
        val operatorToken = checkForOperator()
        if (operatorToken != null) {
            return operatorToken
        }
        val start = position
        if (current.isLetter()) {
            while (current.isLetter()) {
                next()
            }
            val literal = input.substring(start, position)
            return SyntaxToken.keywordToken(start, literal)
        }

        diagnostics.add("Unexpected character", current.toString(), position)
        next()
        return SyntaxToken.badToken(position, current.toString())

    }

    private fun checkForOperator(): SyntaxToken<*>? {
        val start = position
        var operatorPosition = 0
        val maxOperatorLength = Operator.maxOperatorLength
        var lastFoundOperatorPosition = 0

        var next = peek(operatorPosition)
        var operator = ""
        var syntaxToken: SyntaxToken<*>? = null
        do {
            operator += next
            val token = SyntaxToken.operator(start, operator)
            if (token != null) {
                syntaxToken = token
                lastFoundOperatorPosition = operatorPosition
            }
            operatorPosition++
            next = peek(operatorPosition)
        } while (!next.isWhitespace() && operatorPosition < maxOperatorLength);
        if (syntaxToken != null) {
            position += lastFoundOperatorPosition + 1
            return syntaxToken
        }
        return null
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