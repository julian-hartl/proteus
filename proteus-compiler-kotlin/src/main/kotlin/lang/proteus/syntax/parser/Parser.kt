package lang.proteus.syntax.parser

import lang.proteus.binding.types.KotlinBinaryString
import lang.proteus.diagnostics.*
import lang.proteus.symbols.TypeSymbol
import lang.proteus.syntax.lexer.Lexer
import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.*
import lang.proteus.syntax.parser.statements.*

internal class Parser(
    private val syntaxTree: SyntaxTree,
) : Diagnosable {
    private var tokens: Array<SyntaxToken<*>> = arrayOf()
    private var position: Int = 0
    private val diagnosticsBag: DiagnosticsBag = DiagnosticsBag()

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

    constructor(input: String) : this(SyntaxTree.parse(input))

    init {
        val lexer = Lexer(syntaxTree)
        this.tokens = parseInput(lexer)
        diagnosticsBag.concat(lexer.diagnosticsBag)
    }

    internal fun parseCompilationUnit(): CompilationUnitSyntax {
        val members = parseMembers()
        if (members.isEmpty()) {
            val location = TextLocation(syntaxTree.sourceText, TextSpan(0, 0))
            diagnosticsBag.reportExpectedGlobalStatement(location)
        }
        val endOfFileToken = matchToken(Token.EndOfFile)
        return CompilationUnitSyntax(members, endOfFileToken, syntaxTree)
    }

    private fun parseMembers(): List<MemberSyntax> {
        val members = mutableListOf<MemberSyntax>()
        var hasParsedOtherThanImport = false
        while (current.token != Token.EndOfFile) {
            val start = current
            val member = parseMember()

            if (member is ImportStatementSyntax) {
                if (hasParsedOtherThanImport) {
                    diagnosticsBag.reportImportMustBeFirstStatement(member)
                } else {
                    if (!member.file.exists()) {
                        diagnosticsBag.reportImportedFileNotFound(member.resolvedFilePath, member.location)
                    }
                }
            } else {
                hasParsedOtherThanImport = true
            }
            if (member != null) {
                members.add(member)
            }
            if (current == start) {
                nextToken()
            }
        }
        return members
    }

    private fun parseMember(): MemberSyntax? {
        return when (current.token) {
            is Keyword.Import -> parseImportStatement()
            is Keyword.External, is Keyword.Fn -> parseFunctionDeclaration()
            is Keyword.Val, Keyword.Var, Keyword.Const -> parseGlobalVariableDeclaration()
            is Keyword.Struct -> parseStructDeclaration()
            else -> {
                diagnosticsBag.reportUnexpectedToken(current.location,  current.token,Token.GlobalStatement)
                null
            }
        }
    }

    private fun parseStructDeclaration(): MemberSyntax {
        val structKeyword = matchToken(Keyword.Struct)
        val identifier = matchToken(Token.Identifier)
        val openBrace = matchToken(Token.OpenBrace)
        val members = parseStructMembers()
        val closeBrace = matchToken(Token.CloseBrace)
        return StructDeclarationSyntax(
            syntaxTree,
            structKeyword,
            identifier,
            openBrace,
            members,
            closeBrace,
        )
    }

    private fun parseStructMembers(): List<StructMemberSyntax> {
        val members = mutableListOf<StructMemberSyntax>()
        while (current.token != Token.CloseBrace && current.token != Token.EndOfFile) {
            val start = current
            val member = parseStructMember()
            members.add(member)
            if (current == start) {
                nextToken()
            }
        }
        return members
    }

    private fun parseStructMember(): StructMemberSyntax {
        val identifier = matchToken(Token.Identifier)
        val type = parseTypeClause()
        val semiColon = matchToken(Token.SemiColon)
        return StructMemberSyntax(syntaxTree, identifier, type, semiColon)
    }

    private fun parseStructInitialization(identifier: SyntaxToken<Token.Identifier>): StructInitializationExpressionSyntax {
        val openBrace = matchToken(Token.OpenBrace)
        val members = parseStructInitializationMembers()
        val closeBrace = matchToken(Token.CloseBrace)
        return StructInitializationExpressionSyntax(
            syntaxTree,
            identifier,
            openBrace,
            members,
            closeBrace,
        )
    }

    private fun parseStructInitializationMembers(): SeparatedSyntaxList<StructMemberInitializationSyntax> {

        val membersAndSeparators = mutableListOf<SyntaxNode>()
        while (current.token != Token.CloseBrace && current.token != Token.EndOfFile) {
            val start = current
            val member = parseStructInitializationMember()
            membersAndSeparators.add(member)
            if(current.token == Token.CloseBrace) {
                break
            }
            val comma = matchToken(Token.Comma)
            membersAndSeparators.add(comma)
            if (current == start) {
                nextToken()
            }
        }
        return SeparatedSyntaxList(membersAndSeparators)

    }

    private fun parseStructInitializationMember(): StructMemberInitializationSyntax {
        val identifier = matchToken(Token.Identifier)
        val colon = matchToken(Token.Colon)
        val expression = parseExpression()
        return StructMemberInitializationSyntax(syntaxTree, identifier, colon, expression)
    }

    private fun parseImportStatement(): MemberSyntax {
        val importKeyword = matchToken(Keyword.Import)
        val filePath = matchToken(Token.String)
        val semicolon = matchToken(Token.SemiColon)
        val importStatement = ImportStatementSyntax(importKeyword, filePath, semicolon, syntaxTree)
        if (!importStatement.isValidImport) {
            diagnosticsBag.reportInvalidImport(importStatement)
        }
        return importStatement
    }

    private fun parseFunctionDeclaration(): MemberSyntax {
        val functionModifiers = parseFunctionModifiers()
        val functionKeyword = matchToken(Keyword.Fn)
        val identifier = matchToken(Token.Identifier)
        val openParenthesis = matchToken(Operator.OpenParenthesis)
        val parameterList = parseParameterList()
        val closeParenthesis = matchToken(Operator.CloseParenthesis)
        val returnTypeSyntax = parseOptionalFunctionReturnType()
        val isExternal = functionModifiers.contains(Keyword.External)
        val needsBody = !isExternal
        val body = if (!needsBody) null else parseBlockStatement()
        val semiColon = if (needsBody) null else matchToken(Token.SemiColon)
        return FunctionDeclarationSyntax(
            functionModifiers,
            functionKeyword,
            identifier,
            openParenthesis,
            parameterList,
            closeParenthesis,
            returnTypeSyntax,
            body,
            semiColon,
            syntaxTree
        )
    }

    private fun parseFunctionModifiers(): Set<Keyword> {
        val modifiers = mutableSetOf<Keyword>()
        while (current.token is Keyword && current.token != Keyword.Fn) {
            val wasAdded = modifiers.add(current.token as Keyword)
            if (!wasAdded) {
                diagnosticsBag.reportDuplicateModifier(current)
            }
            nextToken()
        }
        return modifiers
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
        val type = parseType()
        return FunctionReturnTypeSyntax(arrow, type, syntaxTree)
    }


    private fun parseParameterList(): SeparatedSyntaxList<ParameterSyntax> {
        val parameters = mutableListOf<SyntaxNode>()
        if (current.token != Operator.CloseParenthesis) {
            do {
                var start = current
                if (parameters.isNotEmpty()) {
                    parameters.add(matchToken(Token.Comma))
                }
                val parameter = parseParameter()
                parameters.add(parameter)
                if (current == start) {
                    nextToken()
                }
            } while (current.token != Operator.CloseParenthesis)
        }
        return SeparatedSyntaxList(parameters)
    }

    private fun parseParameter(): ParameterSyntax {
        val identifier = matchToken(Token.Identifier)
        val typeClauseSyntax = parseTypeClause()
        return ParameterSyntax(identifier, typeClauseSyntax, syntaxTree)
    }

    private fun parseGlobalVariableDeclaration(): GlobalVariableDeclarationSyntax {
        val statement = parseVariableDeclarationStatement()
        val semiColon = matchToken(Token.SemiColon)

        return GlobalVariableDeclarationSyntax(statement, semiColon, syntaxTree)
    }


    private fun parseStatement(): StatementSyntax {
        when (current.token) {
            is Token.OpenBrace -> {
                return parseBlockStatement()
            }

            Keyword.Var, Keyword.Val, Keyword.Const -> {
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

            is Keyword.Continue -> {
                return parseContinueStatement()
            }

            is Keyword.Break -> {
                return parseBreakStatement()
            }

            is Keyword.Return -> {
                return parseReturnStatement()
            }


            else -> return parseExpressionStatement()
        }
    }

    private fun parseReturnStatement(): StatementSyntax {

        val returnKeyword = matchToken(Keyword.Return)

        val statement = if (current.token is Token.SemiColon) null else parseExpression()
        return ReturnStatementSyntax(returnKeyword, statement, syntaxTree)
    }

    private fun parseBreakStatement(): StatementSyntax {
        val breakKeyword = matchToken(Keyword.Break)
        return BreakStatementSyntax(breakKeyword, syntaxTree)
    }

    private fun parseContinueStatement(): StatementSyntax {
        val continueKeyword = matchToken(Keyword.Continue)
        return ContinueStatementSyntax(continueKeyword, syntaxTree)
    }

    private fun parseForStatement(): StatementSyntax {
        val forToken = matchToken(Keyword.For)
        val identifier = matchToken(Token.Identifier)
        val inKeyword = matchToken(Keyword.In)
        val lowerBound = parseExpression()
        val rangeOperator = matchToken(Keyword.Until)
        val upperBound = parseExpression()
        val body = parseStatement()
        return ForStatementSyntax(
            forToken,
            identifier,
            inKeyword,
            lowerBound,
            rangeOperator,
            upperBound,
            body,
            syntaxTree
        )
    }

    private fun parseWhileStatement(): StatementSyntax {
        val whileKeyword = matchToken(Keyword.While)
        val condition = parseExpression()
        val body = parseStatement()
        return WhileStatementSyntax(whileKeyword, condition, body, syntaxTree)
    }

    private fun parseIfStatement(): StatementSyntax {
        val ifKeyword = matchToken(Keyword.If)
        val condition = parseExpression()
        val thenStatement = parseStatement()
        val elseClause = parseElseClause()
        return IfStatementSyntax(ifKeyword, condition, thenStatement, elseClause, syntaxTree)
    }

    private fun parseElseClause() = if (current.token == Keyword.Else) {
        val elseKeyword = matchToken(Keyword.Else)
        val elseStatement = parseStatement()
        ElseClauseSyntax(elseKeyword, elseStatement, syntaxTree)
    } else {
        null
    }

    private fun parseVariableDeclarationStatement(): VariableDeclarationSyntax {
        val keyword = nextToken().token as Keyword
        val identifier = matchToken(Token.Identifier)
        val typeClause = parseOptionalTypeClause()
        val equals = matchToken(Operator.Equals)
        val expression = parseExpression()
        return VariableDeclarationSyntax(keyword, identifier, typeClause, equals, expression, syntaxTree)
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
        val type = parseType()
        return TypeClauseSyntax(colonToken, type, syntaxTree)
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
        return BlockStatementSyntax(openBrace, statements, closeBrace, syntaxTree)
    }

    private fun parseExpressionStatement(): StatementSyntax {
        val expression = parseExpression()
        return ExpressionStatementSyntax(expression, syntaxTree)
    }


    private fun parseExpression(): ExpressionSyntax {
        return parseReferenceExpression()
    }

    private fun parseReferenceExpression(): ExpressionSyntax {
        if(current.token is Operator.Ampersand) {
            val ampersand = matchToken(Operator.Ampersand)
            val expression = parseReferenceExpression()
            return ReferenceExpressionSyntax(ampersand, expression, syntaxTree)
        }
        return parseDereferenceExpression()
    }

    private fun parseDereferenceExpression(): ExpressionSyntax {
        if(current.token is Operator.Asterisk) {
            val asterisk = matchToken(Operator.Asterisk)
            val expression = parseDereferenceExpression()
            return DereferenceExpressionSyntax(asterisk, expression, syntaxTree)
        }
        return parseAssigmentExpression()
    }

    private fun parseAssigmentExpression(): ExpressionSyntax {
        if (peek(0).token is Token.Identifier && peek(1).token is AssignmentOperator) {
            val identifierToken = matchToken(Token.Identifier)
            val assignmentOperator = matchOneToken(Operators.assignmentOperators, expect = Operator.Equals)
            val expression = parseAssigmentExpression()
            return AssignmentExpressionSyntax(
                identifierToken,
                assignmentOperator,
                expression,
                syntaxTree
            )
        }
        return parseBinaryExpression()
    }

    private fun parseTypeCastExpression(expressionToCast: ExpressionSyntax? = null): ExpressionSyntax {
        val castExpression = expressionToCast ?: parseBinaryExpression()
        return CastExpressionSyntax(
            castExpression,
            matchToken(Keyword.As),
            parseType(),
            syntaxTree
        )
    }

    private fun parseType(): TypeSyntax {
        var pointerToken: SyntaxToken<Operator.Ampersand>? = null
        if (current.token is Operator.Ampersand) {
            pointerToken = matchToken(Operator.Ampersand)
        }
        val identifier = matchToken(Token.Identifier)
        return TypeSyntax(pointerToken, identifier, syntaxTree)
    }

    private fun parseBinaryExpression(parentPrecedence: Int = 0): ExpressionSyntax {

        val unaryOperatorPrecedence = currentOperator?.unaryPrecedence() ?: 0
        var left =
            if (unaryOperatorPrecedence != 0 && unaryOperatorPrecedence >= parentPrecedence) {
                val operatorToken = nextToken()
                val operand = parseBinaryExpression(unaryOperatorPrecedence)
                UnaryExpressionSyntax(operatorToken as SyntaxToken<Operator>, operand, syntaxTree)
            } else {
                parsePrimaryExpression()
            }

        while (true) {
            val precedence = currentOperator?.precedence ?: 0

            if (current.token is Keyword.As) {
                return parseTypeCastExpression(left)
            }

            if(current.token is Token.Dot) {
                left = parseMemberAccessExpression(left)
                continue
            }

            if (current.token is Token.SemiColon) {
                break
            }

            if (precedence == 0 || precedence <= parentPrecedence) {
                break
            }

            val operatorToken = nextToken()
            val right = parseBinaryExpression(precedence)
            left = BinaryExpressionSyntax(
                left,
                operatorToken as SyntaxToken<Operator>,
                right,
                syntaxTree
            )
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

            Token.Identifier -> {
                return parseNameOrCallOrStructInitializationExpression()
            }

            Token.Number -> {
                return parseNumberExpression()
            }

            Token.String -> {
                return parseStringExpression()
            }


            else -> {
                diagnosticsBag.reportUnexpectedToken(current.location, current.token, Token.Expression)
                return parseNameOrCallOrStructInitializationExpression()
            }
        }

    }

    private fun parseStringExpression(): ExpressionSyntax {
        val token = matchToken(Token.String)
        return LiteralExpressionSyntax(
            token,
            token.literal,
            syntaxTree
        )
    }

    private fun parseBitStringLiteral(): ExpressionSyntax {
        if (current.literal == "0" && peek(1).literal == "b") {
            val numberToken = matchToken(Token.Number)
            val bToken = matchToken(Token.Identifier)
            if (bToken.literal.length != 1) {
                diagnosticsBag.reportInvalidNumberStringIdentifier(bToken.location, bToken.literal)
            } else {
                val binaryToken = matchToken(Token.Number)
                val binaryString = binaryToken.literal
                if (!isValidBinaryString(binaryString)) {
                    diagnosticsBag.reportInvalidBinaryString(binaryToken.location, binaryString)
                }
                return LiteralExpressionSyntax(
                    numberToken,
                    KotlinBinaryString(binaryString),
                    syntaxTree
                )
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
            diagnosticsBag.reportInvalidCharLiteral(literalToken.literal, literalToken.location)
        }
        matchToken(Token.SingleQuote)
        return LiteralExpressionSyntax(token, chars[0], syntaxTree)
    }

    private fun parseTypeExpression(): LiteralExpressionSyntax {
        val token = current
        nextToken()
        return LiteralExpressionSyntax(token, token.value as TypeSymbol, syntaxTree)
    }

    private fun parseNumberExpression(): LiteralExpressionSyntax {
        val numberToken = matchToken(Token.Number)

        if (numberToken.value !is Int) {
            diagnosticsBag.reportInvalidNumber(
                numberToken.value.toString(),
                numberToken.location,
                TypeSymbol.Int
            )
        }
        return LiteralExpressionSyntax(numberToken, numberToken.value as Int, syntaxTree)
    }

    private fun parseParenthesizedExpression(): ParenthesizedExpressionSyntax {
        val left = nextToken()
        val expression = parseExpression()
        val right = matchToken(Operator.CloseParenthesis)
        return ParenthesizedExpressionSyntax(left, expression, right, syntaxTree)
    }

    private fun parseBooleanLiteral(): LiteralExpressionSyntax {
        val value = current.token == Keyword.True
        val token = current
        nextToken()
        return LiteralExpressionSyntax(token, value, syntaxTree)
    }

    private fun parseNameOrCallOrStructInitializationExpression(): ExpressionSyntax {
        val token = matchToken(Token.Identifier)
        if (current.token is Operator.OpenParenthesis
        ) {
            return parseCallExpression(token);
        }
        if (current.token is Token.OpenBrace) {
            return parseStructInitialization(token)
        }
        if(current.token is Token.Dot){
            return parseMemberAccessExpression(parseNameExpression(token))
        }

        return parseNameExpression(token)
    }

    private fun parseCallExpression(token: SyntaxToken<Token.Identifier>): ExpressionSyntax {
        val openParenthesis = matchToken(Operator.OpenParenthesis)
        val arguments = parseArguments()
        val closeParenthesis = matchToken(Operator.CloseParenthesis)
        if (syntaxTree.id == 0 && token.literal == "main") {
            diagnosticsBag.reportCannotCallMain(token.location)
        }
        return CallExpressionSyntax(token, openParenthesis, arguments, closeParenthesis, syntaxTree)
    }

    private fun parseArguments(): SeparatedSyntaxList<ExpressionSyntax> {
        val nodesAndSeparators = mutableListOf<SyntaxNode>()

        var lastToken: SyntaxNode? = null

        while ((lastToken == null && current.token !is Operator.CloseParenthesis) || lastToken?.token is Token.Comma && current.token !is Token.EndOfFile) {
            val expression = parseExpression()
            nodesAndSeparators.add(expression)
            lastToken = expression
            if (current.token !is Operator.CloseParenthesis) {
                val diagnosticSize = diagnosticsBag.diagnostics.errors.size
                val comma = matchToken(Token.Comma)
                val newDiagnosticSize = diagnosticsBag.diagnostics.errors.size
                if (diagnosticSize == newDiagnosticSize) {
                    nodesAndSeparators.add(comma)
                    lastToken = comma
                }
            }
        }

        return SeparatedSyntaxList(nodesAndSeparators)
    }

    private fun parseMemberAccessExpression(left: ExpressionSyntax): ExpressionSyntax {
        val dotToken = matchToken(Token.Dot)
        val name = matchToken(Token.Identifier)
        return MemberAccessExpressionSyntax(syntaxTree, left, dotToken, name)
    }

    private fun parseNameExpression(token: SyntaxToken<Token.Identifier>) =
        NameExpressionSyntax(token, syntaxTree)

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

            current.location,
            actual = current.token,
            expected = token,
        )
        return SyntaxToken(token, current.position, current.literal, null, syntaxTree)
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