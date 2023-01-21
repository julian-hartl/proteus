package lang.proteus.syntax.parser

import lang.proteus.binding.types.KotlinBinaryString
import lang.proteus.diagnostics.Diagnosable
import lang.proteus.diagnostics.Diagnostics
import lang.proteus.diagnostics.DiagnosticsBag
import lang.proteus.symbols.TypeSymbol
import lang.proteus.syntax.lexer.Lexer
import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.*
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
        val members = parseMembers()
        val endOfFileToken = matchToken(Token.EndOfFile)
        return CompilationUnitSyntax(members, endOfFileToken)
    }

    private fun parseMembers(): List<MemberSyntax> {
        val members = mutableListOf<MemberSyntax>()
        while (current.token != Token.EndOfFile) {
            val start = current
            val member = parseMember()
            members.add(member)
            if (current == start) {
                nextToken()
            }
        }
        return members
    }

    private fun parseMember(): MemberSyntax {
        return when (current.token) {

            is Keyword.Fn -> parseFunctionDeclaration()
            else -> parseGlobalStatement()
        }
    }

    private fun parseFunctionDeclaration(): MemberSyntax {
        val functionKeyword = matchToken(Keyword.Fn)
        val identifier = matchToken(Token.Identifier)
        val openParenthesis = matchToken(Operator.OpenParenthesis)
        val parameterList = parseParameterList()
        val closeParenthesis = matchToken(Operator.CloseParenthesis)
        val returnTypeSyntax = parseOptionalFunctionReturnType()
        val body = parseBlockStatement()
        return FunctionDeclarationSyntax(
            functionKeyword,
            identifier,
            openParenthesis,
            parameterList,
            closeParenthesis,
            returnTypeSyntax,
            body
        )
    }

    private fun parseOptionalFunctionReturnType(): FunctionReturnTypeSyntax? {
        return if (current.token == Token.Arrow) {
            parseFunctionReturnType()
        } else {
            null
        }
    }

    private fun parseFunctionReturnType(): FunctionReturnTypeSyntax {
        val arrow = matchToken(Token.Arrow)
        val type = matchToken(Token.Type)
        return FunctionReturnTypeSyntax(arrow, type)
    }


    private fun parseParameterList(): SeparatedSyntaxList<ParameterSyntax> {
        val parameters = mutableListOf<SyntaxNode>()
        if (current.token != Operator.CloseParenthesis) {
            do {
                val parameter = parseParameter()
                parameters.add(parameter)
                if (current.token is Token.Comma) {
                    parameters.add(matchToken(Token.Comma))
                }
            } while (current.token == Token.Comma)
        }
        return SeparatedSyntaxList(parameters)
    }

    private fun parseParameter(): ParameterSyntax {
        val identifier = matchToken(Token.Identifier)
        val typeClauseSyntax = parseTypeClause()
        return ParameterSyntax(identifier, typeClauseSyntax)
    }

    private fun parseGlobalStatement(): GlobalStatementSyntax {
        val statement = parseStatement()
        val semiColon = matchToken(Token.SemiColon)
        return GlobalStatementSyntax(statement, semiColon)
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
        val lowerBound = parseExpression()
        val rangeOperator = matchToken(Keyword.Until)
        val upperBound = parseExpression()
        val body = parseStatement()
        return ForStatementSyntax(forToken, identifier, inKeyword, lowerBound, rangeOperator, upperBound, body)
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
        val typeClause = parseOptionalTypeClause()
        val equals = matchToken(Operator.Equals)
        val expression = parseExpression()
        return VariableDeclarationSyntax(keyword, identifier, typeClause, equals, expression)
    }

    private fun parseOptionalTypeClause(): TypeClauseSyntax? {
        return if (current.token == Token.Colon) {
            parseTypeClause()
        } else {
            null
        }

    }

    private fun parseTypeClause(): TypeClauseSyntax {
        val colonToken = matchToken(Token.Colon)
        val type = matchToken(Token.Type)
        return TypeClauseSyntax(colonToken, type)
    }

    private fun parseBlockStatement(): BlockStatementSyntax {
        val openBrace = matchToken(Token.OpenBrace)
        val statements = mutableListOf<StatementSyntax>()
        while (current.token !is Token.CloseBrace && current.token !is Token.EndOfFile) {
            val startToken = current
            val statement = parseStatement()
            if (statement !is BlockStatementSyntax) {
                if (peek(-1).token is Token.CloseBrace) {
                    if (current.token is Token.SemiColon)
                        nextToken()
                } else {
                    matchToken(Token.SemiColon)
                }
            }
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
        if (peek(0).token is Token.Identifier && peek(1).token is AssignmentOperator) {
            val identifierToken = matchToken(Token.Identifier)
            val assignmentOperator = matchOneToken(Operators.assignmentOperators, expect = Operator.Equals)
            val expression = parseAssigmentExpression()
            return AssignmentExpressionSyntax(identifierToken, assignmentOperator, expression)
        }
        if (peek(1).token is Keyword.As) {
            return parseTypeCastExpression()
        }
        return parseBinaryExpression()
    }

    private fun parseTypeCastExpression(expressionToCast: ExpressionSyntax? = null): ExpressionSyntax {
        val castExpression = expressionToCast ?: parseBinaryExpression()
        return CastExpressionSyntax(
            castExpression,
            matchToken(Keyword.As),
            matchToken(Token.Type)
        )
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

            if(current.token is Keyword.As){
                return parseTypeCastExpression(left)
            }

            if (current.token is Token.SemiColon) {
                break
            }

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

            Keyword.False, Keyword.True -> {
                return parseBooleanLiteral()
            }

            Token.Type -> {
                return parseTypeExpression()
            }

            Token.Identifier -> {
                return parseNameOrCallExpression()
            }

            Token.Number -> {
                return parseNumberExpression()
            }

            Token.String -> {
                return parseStringExpression()
            }


            else -> {
                diagnosticsBag.reportUnexpectedToken(current.span(), current.token, Token.Expression)
                return parseNameOrCallExpression()
            }
        }

    }

    private fun parseStringExpression(): ExpressionSyntax {
        val token = matchToken(Token.String)
        return LiteralExpressionSyntax(token, token.literal)
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

    private fun parseTypeExpression(): LiteralExpressionSyntax {
        val token = current
        nextToken()
        return LiteralExpressionSyntax(token, token.value as TypeSymbol)
    }

    private fun parseNumberExpression(): LiteralExpressionSyntax {
        val numberToken = matchToken(Token.Number)

        if (numberToken.value !is Int) {
            diagnosticsBag.reportInvalidNumber(
                numberToken.value.toString(),
                numberToken.span(),
                TypeSymbol.Int
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

    private fun parseNameOrCallExpression(): ExpressionSyntax {
        val token = matchToken(Token.Identifier)
        if (current.token is Operator.OpenParenthesis
        ) {
            return parseCallExpression(token);
        }

        return parseNameExpression(token)
    }

    private fun parseCallExpression(token: SyntaxToken<Token.Identifier>): ExpressionSyntax {
        val openParenthesis = matchToken(Operator.OpenParenthesis)
        val arguments = parseArguments()
        val closeParenthesis = matchToken(Operator.CloseParenthesis)
        return CallExpressionSyntax(token, openParenthesis, arguments, closeParenthesis)
    }

    private fun parseArguments(): SeparatedSyntaxList<ExpressionSyntax> {
        val nodesAndSeparators = mutableListOf<SyntaxNode>()

        while (current.token !is Operator.CloseParenthesis && current.token !is Token.EndOfFile) {
            val expression = parseExpression()
            nodesAndSeparators.add(expression)
            if (current.token !is Operator.CloseParenthesis) {
                val comma = matchToken(Token.Comma)
                nodesAndSeparators.add(comma)
            }
        }

        return SeparatedSyntaxList(nodesAndSeparators)
    }

    private fun parseNameExpression(token: SyntaxToken<Token.Identifier>) =
        NameExpressionSyntax(token)

    private fun <T : Token> matchOneToken(tokens: List<T>, expect: T? = null): SyntaxToken<T> {
        for (token in tokens) {
            if (current.token == token) {
                return matchToken(token)
            }
        }
        return matchToken(expect ?: tokens[0])
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