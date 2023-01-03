package syntax.parser

import diagnostics.Diagnosable
import diagnostics.Diagnostics
import syntax.lexer.Lexer
import syntax.lexer.Operator
import syntax.lexer.SyntaxKind
import syntax.lexer.SyntaxToken

class Parser private constructor(
    private val input: String,
    private var tokens: Array<SyntaxToken<*>>,
    private var position: Int,
    private val verbose: Boolean,
    private var diagnostics: Diagnostics
) : Diagnosable {


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
        this.tokens = parseInput(lexer)
        diagnostics = lexer.diagnostics
    }

    fun parse(): SyntaxTree {
        printInput()
        printLexerTokens()
        val expression = parseExpression()
        val endOfFileToken = matchToken(SyntaxKind.EndOfFileToken)

        return SyntaxTree(expression, endOfFileToken, diagnostics)
    }

    private fun parseExpression(parentPrecedence: Int = 0): ExpressionSyntax {

        val unaryOperatorPrecedence = currentOperator?.unaryPrecedence() ?: 0
        var left =
            if (unaryOperatorPrecedence != 0 && unaryOperatorPrecedence >= parentPrecedence) {
                val operatorToken = nextToken()
                val operand = parseExpression(unaryOperatorPrecedence)
                UnaryExpressionSyntax(operatorToken, operand)
            } else {
                parsePrimaryExpression()
            }

        while (true) {
            val precedence = currentOperator?.precedence ?: 0

            if (precedence == 0 || precedence <= parentPrecedence) {
                break
            }

            val operatorToken = nextToken()
            val right = parseExpression(precedence)
            left = BinaryExpression(left, operatorToken, right)
        }

        return left
    }

    private val currentOperator
        get() = Operator.fromLiteral(current.literal)

    private fun printLexerTokens() {
        if (verbose)
            println("Lexer tokens: ${tokens.map { it.literal }}")
    }

    private fun printInput() {
        if (verbose)
            println("Input: $input")
    }

    private fun parsePrimaryExpression(): ExpressionSyntax {

        when (current.kind) {
            SyntaxKind.OpenParenthesisToken -> {
                val left = nextToken()
                val expression = parseExpression()
                val right = matchToken(SyntaxKind.CloseParenthesisToken)
                return ParenthesizedExpressionSyntax(left, expression, right)
            }

            SyntaxKind.FalseKeyword, SyntaxKind.TrueKeyword -> {
                val value = current.kind == SyntaxKind.TrueKeyword
                val token = current
                nextToken()
                return LiteralExpressionSyntax(token, value)
            }

            else -> {
                val numberToken = matchToken(SyntaxKind.NumberToken)

                if (numberToken.value !is Int) {
                    diagnostics.add(
                        "The number ${numberToken.literal} isn't valid Int32.",
                        numberToken.literal,
                        numberToken.position
                    )
                }
                return LiteralExpressionSyntax(numberToken, numberToken.value as Int)
            }
        }

    }

    private fun matchToken(syntaxKind: SyntaxKind): SyntaxToken<*> {
        if (current.kind == syntaxKind) {
            return nextToken()
        }
        diagnostics.add(
            "Unexpected token <${current.kind}>, expected <$syntaxKind>",
            current.literal,
            current.position
        )
        return SyntaxToken(syntaxKind, current.position, current.literal, null)
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

    override fun printDiagnostics() {
        diagnostics.print()
    }

    override fun hasErrors(): Boolean {
        return diagnostics.size() > 0
    }

}