package lang.proteus.syntax.parser

import lang.proteus.binding.ProteusType
import lang.proteus.binding.types.KotlinBinaryString
import lang.proteus.diagnostics.Diagnosable
import lang.proteus.diagnostics.Diagnostics
import lang.proteus.diagnostics.DiagnosticsBag
import lang.proteus.syntax.lexer.Lexer
import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Keyword
import lang.proteus.syntax.lexer.token.Operator
import lang.proteus.syntax.lexer.token.Operators
import lang.proteus.syntax.lexer.token.Token
import lang.proteus.syntax.parser.statements.*
import lang.proteus.text.SourceText

class Parser private constructor(
    private val input: SourceText,
    private var tokens: Array<SyntaxToken<*>>,
    private var position: Int,
    private val diagnosticsBag: DiagnosticsBag,
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

    }

    constructor(input: String) : this(SourceText.from(input))

    constructor(sourceText: SourceText) : this(sourceText, arrayOf(), 0, DiagnosticsBag()) {
        val lexer = Lexer(sourceText)
        this.tokens = parseInput(lexer)
        diagnosticsBag.concat(lexer.diagnosticsBag)
    }

    internal fun parseCompilationUnit(): CompilationUnitSyntax {
        val expression = parseStatement()
        val endOfFileToken = matchToken(Token.EndOfFile)
        return CompilationUnitSyntax(expression, endOfFileToken)
    }

    private fun parseStatement(): StatementSyntax {
        when (current.token) {
            is Token.OpenBrace -> {
                return parseBlockStatement()
            }

            Keyword.Var, Keyword.Val -> {
                return parseVariableDeclarationStatement()
            }

            is Keyword.If -> {
                return parseIfStatement()
            }

            is Keyword.While -> {
                return parseWhileStatement()
            }

            is Keyword.For -> {
                return parseForStatement()
            }

            else -> return parseExpressionStatement()
        }
    }

    private fun parseForStatement(): StatementSyntax {
        val forToken = matchToken(Keyword.For)
        val identifier = matchToken(Token.Identifier)
        val inKeyword = matchToken(Keyword.In)
        val iteratorExpression = parseExpression()
        val body = parseStatement()
        return ForStatementSyntax(forToken, identifier, inKeyword, iteratorExpression, body)
    }

    private fun parseWhileStatement(): StatementSyntax {
        val whileKeyword = matchToken(Keyword.While)
        val condition = parseExpression()
        val body = parseStatement()
        return WhileStatementSyntax(whileKeyword, condition, body)
    }

    private fun parseIfStatement(): StatementSyntax {
        val ifKeyword = matchToken(Keyword.If)
        val condition = parseExpression()
        val thenStatement = parseStatement()
        val elseClause = parseElseClause()
        return IfStatementSyntax(ifKeyword, condition, thenStatement, elseClause)
    }

    private fun parseElseClause() = if (current.token == Keyword.Else) {
        val elseKeyword = matchToken(Keyword.Else)
        val elseStatement = parseStatement()
        ElseClauseSyntax(elseKeyword, elseStatement)
    } else {
        null
    }

    private fun parseVariableDeclarationStatement(): StatementSyntax {
        val keyword = nextToken().token as Keyword
        val identifier = matchToken(Token.Identifier)
        val equals = matchToken(Operator.Equals)
        val expression = parseExpression()
        return VariableDeclarationSyntax(keyword, identifier, equals, expression)
    }

    private fun parseBlockStatement(): StatementSyntax {
        val openBrace = matchToken(Token.OpenBrace)
        val statements = mutableListOf<StatementSyntax>()
        while (current.token !is Token.CloseBrace && current.token !is Token.EndOfFile) {
            val startToken = current
            val statement = parseStatement()
            statements.add(statement)
            // If we didn't consume any tokens, we're in an infinite loop.
            if (startToken == current) {
                nextToken()
            }
        }
        val closeBrace = matchToken(Token.CloseBrace)
        return BlockStatementSyntax(openBrace, statements, closeBrace)
    }

    private fun parseExpressionStatement(): StatementSyntax {
        val expression = parseExpression()
        return ExpressionStatementSyntax(expression)
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


    private fun parsePrimaryExpression(): ExpressionSyntax {
        when (current.token) {
            Operator.OpenParenthesis -> {
                return parseParenthesizedExpression()
            }

            Token.SingleQuote -> {
                return parseCharLiteralExpression()
            }

            Token.QuotationMark -> {
                return parseStringLiteralExpression()
            }

            Keyword.False, Keyword.True -> {
                return parseBooleanLiteral()
            }

            Token.Type -> {
                return parseTypeExpression()
            }

            Token.Identifier -> {
                return parseNameExpression()
            }

            Token.Number -> {
                return parseNumberExpression()
            }

            else -> {
                diagnosticsBag.reportUnexpectedToken(current.span(), current.token, Token.Expression)
                return parseNameExpression()
            }
        }

    }

    private fun parseBitStringLiteral(): ExpressionSyntax {
        if (current.literal == "0" && peek(1).literal == "b") {
            val numberToken = matchToken(Token.Number)
            val bToken = matchToken(Token.Identifier)
            if (bToken.literal.length != 1) {
                diagnosticsBag.reportInvalidNumberStringIdentifier(bToken.span(), bToken.literal)
            } else {
                val binaryToken = matchToken(Token.Number)
                val binaryString = binaryToken.literal
                if (!isValidBinaryString(binaryString)) {
                    diagnosticsBag.reportInvalidBinaryString(binaryToken.span(), binaryString)
                }
                return LiteralExpressionSyntax(numberToken, KotlinBinaryString(binaryString))
            }
        }
        return parseNumberExpression()
    }

    private fun isValidBinaryString(binaryString: String): Boolean {
        for (char in binaryString) {
            if (char != '0' && char != '1') {
                return false
            }
        }
        return true
    }

    private fun parseCharLiteralExpression(): ExpressionSyntax {
        val token = matchToken(Token.SingleQuote)
        val literalToken = nextToken()
        val chars = literalToken.literal.toCharArray()
        if (chars.size != 1) {
            diagnosticsBag.reportInvalidCharLiteral(literalToken.literal, literalToken.span())
        }
        matchToken(Token.SingleQuote)
        return LiteralExpressionSyntax(token, chars[0])
    }

    private fun parseStringLiteralExpression(): ExpressionSyntax {
        val token = matchToken(Token.QuotationMark)
        val expression = nextToken()
        if (expression.token is Token.QuotationMark) {
            return LiteralExpressionSyntax(token, "")
        }
        matchToken(Token.QuotationMark)
        return LiteralExpressionSyntax(token, expression.literal)
    }

    private fun parseTypeExpression(): LiteralExpressionSyntax {
        val token = current
        nextToken()
        return LiteralExpressionSyntax(token, token.value as ProteusType)
    }

    private fun parseNumberExpression(): LiteralExpressionSyntax {
        val numberToken = matchToken(Token.Number)

        if (numberToken.value !is Int) {
            diagnosticsBag.reportInvalidNumber(
                numberToken.value.toString(),
                numberToken.span(),
                ProteusType.Int
            )
        }
        return LiteralExpressionSyntax(numberToken, numberToken.value as Int)
    }

    private fun parseParenthesizedExpression(): ParenthesizedExpressionSyntax {
        val left = nextToken()
        val expression = parseExpression()
        val right = matchToken(Operator.CloseParenthesis)
        return ParenthesizedExpressionSyntax(left, expression, right)
    }

    private fun parseBooleanLiteral(): LiteralExpressionSyntax {
        val value = current.token == Keyword.True
        val token = current
        nextToken()
        return LiteralExpressionSyntax(token, value)
    }

    private fun parseNameExpression(): NameExpressionSyntax {
        val token = matchToken(Token.Identifier)

        return NameExpressionSyntax(token)
    }

    private fun <T : Token> matchToken(token: T): SyntaxToken<T> {
        if (current.token == token) {
            return nextToken() as SyntaxToken<T>
        }
        diagnosticsBag.reportUnexpectedToken(

            current.span(),
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