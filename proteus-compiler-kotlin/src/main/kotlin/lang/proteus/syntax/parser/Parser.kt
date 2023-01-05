package lang.proteus.syntax.parser

import lang.proteus.binding.ProteusType
import lang.proteus.diagnostics.Diagnosable
import lang.proteus.diagnostics.Diagnostics
import lang.proteus.diagnostics.DiagnosticsBag
import lang.proteus.syntax.lexer.*

class Parser private constructor(
    private val input: String,
    private var tokens: Array<SyntaxToken<*>>,
    private var position: Int,
    private val verbose: Boolean,
    private val diagnosticsBag: DiagnosticsBag
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

    constructor(input: String, verbose: Boolean = false) : this(input, arrayOf(), 0, verbose, DiagnosticsBag()) {
        val lexer = Lexer(input)
        this.tokens = parseInput(lexer)
        diagnosticsBag.concat(lexer.diagnosticsBag)
    }

    fun parse(): SyntaxTree {
        printInput()
        printLexerTokens()
        val expression = parseExpression()
        val endOfFileToken = matchToken(Token.EndOfFile)

        return SyntaxTree(expression, endOfFileToken, diagnosticsBag.diagnostics)
    }

    private fun parseExpression(): ExpressionSyntax {
        return parseAssigmentExpression()
    }

    private fun parseAssigmentExpression(): ExpressionSyntax {
        if (peek(0).token is Token.Identifier && peek(1).token is Operator.Equals) {
            val identifierToken = matchToken(Token.Identifier)
            val equalsToken = matchToken(Operator.Equals)
            val expression = parseAssigmentExpression()
            return AssignmentExpressionSyntax(identifierToken, equalsToken, expression)
        }
        return parseBinaryExpression()
    }

    private fun parseBinaryExpression(parentPrecedence: Int = 0): ExpressionSyntax {

        val unaryOperatorPrecedence = currentOperator?.unaryPrecedence() ?: 0
        var left =
            if (unaryOperatorPrecedence != 0 && unaryOperatorPrecedence >= parentPrecedence) {
                val operatorToken = nextToken()
                val operand = parseBinaryExpression(unaryOperatorPrecedence)
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
            val right = parseBinaryExpression(precedence)
            left = BinaryExpressionSyntax(left, operatorToken as SyntaxToken<Operator>, right)
        }

        return left
    }

    private val currentOperator
        get() = Operators.fromLiteral(current.literal)

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
                return LiteralExpressionSyntax(token, token.value as ProteusType)
            }

            Token.Identifier -> {
                val token = nextToken()

                return NameExpressionSyntax(token as SyntaxToken<Token.Identifier>)
            }

            Token.Number -> {
                val numberToken = matchToken(Token.Number)

                if (numberToken.value !is Int) {
                    diagnosticsBag.reportInvalidNumber(
                        numberToken.span,
                        ProteusType.Int
                    )
                }
                return LiteralExpressionSyntax(numberToken, numberToken.value as Int)
            }

            else -> {
                diagnosticsBag.reportUnexpectedToken(current.span, current.token, Token.Expression)
                return LiteralExpressionSyntax(current, -1)
            }
        }

    }

    private fun <T : Token> matchToken(token: T): SyntaxToken<T> {
        if (current.token == token) {
            return nextToken() as SyntaxToken<T>
        }
        diagnosticsBag.reportUnexpectedToken(

            current.span,
            actual = current.token,
            expected = token,
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
        get() = diagnosticsBag.diagnostics

}