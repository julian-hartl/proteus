package parser

import diagnostics.Diagnostics
import lexer.*

class Parser private constructor(
    private val input: String,
    private var tokens: Array<SyntaxToken<*>>,
    private var position: Int,
    private val verbose: Boolean,
    private var diagnostics: Diagnostics
) {


    companion object {
        private fun parseInput(lexer: Lexer): Array<SyntaxToken<*>> {
            val tokens = mutableListOf<SyntaxToken<*>>()
            var token: SyntaxToken<*>
            do {
                token = lexer.nextToken()
                if (token.kind != SyntaxKind.WhiteSpaceToken && token.kind != SyntaxKind.BadToken) {
                    tokens.add(token)
                }
            } while (token.kind != SyntaxKind.EndOfFileToken)

            return tokens.toTypedArray()
        }

        fun verbose(input: String): Parser {
            return Parser(input, true)
        }
    }

    constructor(input: String, verbose: Boolean = false) : this(input, arrayOf(), 0, verbose, Diagnostics()) {
        val lexer = Lexer(input)
        val tokens = parseInput(lexer)
        this.tokens = tokens
        diagnostics = lexer.diagnostics


    }

    fun parse(): SyntaxTree {
        printInput()
        printLexerTokens()
        val expression = parseBooleanExpression()
        val endOfFileToken = matchToken(SyntaxKind.EndOfFileToken)

        return SyntaxTree(expression, endOfFileToken, diagnostics)
    }

    private fun parseBooleanExpression(): ExpressionSyntax {
        var left = parseTerm()
        while (current.kind.isBooleanOperator()) {
            val operatorToken = nextToken()
            val right = parseTerm()
            left = BinaryExpression(left, operatorToken, right)
        }
        return left;
    }

    private fun parseTerm(): ExpressionSyntax {
        var left = parseFactor()
        while (current.kind.isLowPriorityBinaryOperator()) {
            val operatorToken = nextToken()
            val right = parseFactor()
            left = BinaryExpression(left, operatorToken, right)
        }

        return left;
    }

    private fun parseFactor(): ExpressionSyntax {
        var left = parsePrimaryExpression()
        while (current.kind.isHighPriorityBinaryOperator()) {
            val operatorToken = nextToken()
            val right = parsePrimaryExpression()
            left = BinaryExpression(left, operatorToken, right)
        }

        return left;
    }


    private fun printLexerTokens() {
        if (verbose)
            println("Lexer tokens: ${tokens.map { it.literal }}")
    }

    private fun printInput() {
        if (verbose)
            println("Input: $input")
    }

    private fun parsePrimaryExpression(): ExpressionSyntax {

        if (current.kind == SyntaxKind.OpenParenthesisToken) {
            val left = nextToken()
            val expression = parseTerm()
            val right = matchToken(SyntaxKind.CloseParenthesisToken)
            return ParenthesizedExpressionSyntax(left, expression, right)
        }

        val numberToken = matchToken(SyntaxKind.NumberToken)

        if (numberToken.value !is Int) {
            diagnostics.add(
                "The number ${numberToken.literal} isn't valid Int32.",
                numberToken.literal ?: "",
                numberToken.position
            )
        }
        return LiteralExpressionSyntax(numberToken)
    }

    private fun matchToken(syntaxKind: SyntaxKind): SyntaxToken<*> {
        if (current.kind == syntaxKind) {
            return nextToken()
        }
        diagnostics.add(
            "Unexpected token <${current.kind}>, expected <$syntaxKind>",
            current.literal ?: "<Unknown token>",
            current.position
        )
        return SyntaxToken(syntaxKind, current.position, null, null)
    }

    private fun nextToken(): SyntaxToken<*> {
        val syntaxToken = current
        position++
        return syntaxToken
    }


    private fun peek(offset: Int): SyntaxToken<*> {
        val index = position + offset
        if (index >= tokens.size) {
            return tokens[tokens.size - 1]
        }
        return tokens[index]
    }

    private val current: SyntaxToken<*>
        get() = peek(0)

}