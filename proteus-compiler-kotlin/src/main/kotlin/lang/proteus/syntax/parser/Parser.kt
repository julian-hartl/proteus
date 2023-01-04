package lang.proteus.syntax.parser

import lang.proteus.binding.BoundType
import lang.proteus.diagnostics.Diagnosable
import lang.proteus.diagnostics.Diagnostics
import lang.proteus.diagnostics.MutableDiagnostics
import lang.proteus.syntax.lexer.*

class Parser private constructor(
    private val input: String,
    private var tokens: Array<SyntaxToken<*>>,
    private var position: Int,
    private val verbose: Boolean,
    private var mutableDiagnostics: MutableDiagnostics
) : Diagnosable {


    companion object {
        private fun parseInput(lexer: Lexer): Array<SyntaxToken<*>> {
            val syntaxTokens = mutableListOf<SyntaxToken<*>>()
            var syntaxToken: SyntaxToken<*>
            do {
                syntaxToken = lexer.nextToken()
                if (syntaxToken.token != Token.Whitespace && syntaxToken.token != Token.Bad) {
                    syntaxTokens.add(syntaxToken)
                }
            } while (syntaxToken.token != Token.EndOfFile)

            return syntaxTokens.toTypedArray()
        }

        fun verbose(input: String): Parser {
            return Parser(input, true)
        }
    }

    constructor(input: String, verbose: Boolean = false) : this(input, arrayOf(), 0, verbose, MutableDiagnostics()) {
        val lexer = Lexer(input)
        this.tokens = parseInput(lexer)
        mutableDiagnostics = lexer.diagnostics
    }

    fun parse(): SyntaxTree {
        printInput()
        printLexerTokens()
        val expression = parseExpression()
        val endOfFileToken = matchToken(Token.EndOfFile)

        return SyntaxTree(expression, endOfFileToken, mutableDiagnostics)
    }

    private fun parseExpression(parentPrecedence: Int = 0): ExpressionSyntax {

        val unaryOperatorPrecedence = currentOperator?.unaryPrecedence() ?: 0
        var left =
            if (unaryOperatorPrecedence != 0 && unaryOperatorPrecedence >= parentPrecedence) {
                val operatorToken = nextToken()
                val operand = parseExpression(unaryOperatorPrecedence)
                UnaryExpressionSyntax(operatorToken as SyntaxToken<Operator>, operand)
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
            left = BinaryExpressionSyntax(left, operatorToken as SyntaxToken<Operator>, right)
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
        when (current.token) {
            Operator.OpenParenthesis -> {
                val left = nextToken()
                val expression = parseExpression()
                val right = matchToken(Operator.CloseParenthesis)
                return ParenthesizedExpressionSyntax(left, expression, right)
            }

            Keyword.False, Keyword.True -> {
                val value = current.token == Keyword.True
                val token = current
                nextToken()
                return LiteralExpressionSyntax(token, value)
            }

            Token.Type -> {
                val token = current
                nextToken()
                return LiteralExpressionSyntax(token, token.value as BoundType)
            }

            Token.Identifier -> {
                val token = current
                nextToken()
                return IdentifierExpressionSyntax(token as SyntaxToken<Token.Identifier>)
            }

            else -> {
                val numberToken = matchToken(Token.Number)

                if (numberToken.value !is Int) {
                    mutableDiagnostics.add(
                        "The number ${numberToken.literal} isn't valid Int32.",
                        numberToken.literal,
                        numberToken.position
                    )
                }
                return LiteralExpressionSyntax(numberToken, numberToken.value as Int)
            }
        }

    }

    private fun <T : Token> matchToken(token: T): SyntaxToken<T> {
        if (current.token == token) {
            return nextToken() as SyntaxToken<T>
        }
        mutableDiagnostics.add(
            "Unexpected token <${current.token}>, expected <$token>",
            current.literal,
            current.position
        )
        return SyntaxToken(token, current.position, current.literal, null)
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


    override val diagnostics: Diagnostics
        get() = mutableDiagnostics

}