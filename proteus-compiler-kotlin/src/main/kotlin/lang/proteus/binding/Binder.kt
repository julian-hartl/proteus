package lang.proteus.binding

import lang.proteus.diagnostics.Diagnosable
import lang.proteus.diagnostics.DiagnosticsBag
import lang.proteus.diagnostics.TextSpan
import lang.proteus.generation.Lowerer
import lang.proteus.generation.Optimizer
import lang.proteus.symbols.*
import lang.proteus.syntax.lexer.token.Keyword
import lang.proteus.syntax.parser.*
import lang.proteus.syntax.parser.statements.*
import java.util.*

internal class Binder(private var scope: BoundScope, private val function: FunctionSymbol?) : Diagnosable {


    init {
        if (function != null) {
            for (parameter in function.parameters) {
                scope.tryDeclareVariable(parameter)
            }
        }
    }

    private val controlStructureStack: Stack<Keyword> = Stack()


    companion object {
        fun bindGlobalScope(previous: BoundGlobalScope?, syntax: CompilationUnitSyntax): BoundGlobalScope {
            val parentScope = createParentScopes(previous)
            val binder = Binder(parentScope ?: BoundScope(null), null)
            val statements = mutableListOf<BoundStatement>()
            val diagnostics = DiagnosticsBag()
            for (member in syntax.members) {
                when (member) {
                    is FunctionDeclarationSyntax -> {
                        binder.bindFunctionDeclaration(member)
                    }

                    is GlobalStatementSyntax -> {
                        val statement = binder.bindStatement(member.statement)
                        statements.add(statement)
                    }
                }
            }
            diagnostics.addAll(binder.diagnostics)
            val statement = BoundBlockStatement(statements)
            val variables = binder.scope.getDeclaredVariables()
            val functions = binder.scope.getDeclaredFunctions()
            if (previous != null) {
                diagnostics.addAll(previous.diagnostics)
            }
            return BoundGlobalScope(previous, diagnostics.diagnostics, functions, variables, statement)
        }

        private fun createParentScopes(scope: BoundGlobalScope?): BoundScope? {
            var previous: BoundGlobalScope? = scope
            val stack = Stack<BoundGlobalScope>()
            while (previous != null) {
                stack.push(previous)
                previous = previous.previous
            }

            var parent: BoundScope? = null

            while (stack.size > 0) {
                previous = stack.pop()
                val scope = BoundScope(parent)
                for (variable in previous.variables) {
                    scope.tryDeclareVariable(variable)
                }
                for (function in previous.functions) {
                    scope.tryDeclareFunction(function)
                }

                parent = scope
            }
            return parent;
        }

        fun bindProgram(globalScope: BoundGlobalScope, optimize: Boolean = true): BoundProgram {

            val parentScope = createParentScopes(globalScope)

            val functionBodies = mutableMapOf<FunctionSymbol, BoundBlockStatement>()

            val diagnostics = DiagnosticsBag()
            diagnostics.addAll(globalScope.diagnostics)


            for (function in globalScope.functions) {

                val binder = Binder(parentScope ?: BoundScope(null), function)
                val body = binder.bindStatement(function.declaration!!.body)
                if (!binder.hasErrors()) {
                    val loweredBody = Lowerer.lower(body)
                    val optimizedBody = if (optimize) Optimizer.optimize(loweredBody) else loweredBody
                    functionBodies[function] = optimizedBody
                }
                diagnostics.addAll(binder.diagnostics)
            }


            return BoundProgram(globalScope, diagnostics.diagnostics, functionBodies)
        }

        private fun validateMainFunction(mainFunction: FunctionSymbol?, diagnostics: DiagnosticsBag) {
            if (mainFunction == null) {
                throw IllegalStateException("No main function found")
            } else if (mainFunction.parameters.isNotEmpty()) {
                diagnostics.reportMainMustHaveNoParameters(mainFunction)
            }
//            else if (mainFunction.returnType != TypeSymbol.Int) {
//                diagnostics.reportMainMustHaveIntReturnType(mainFunction)
//            }
        }
    }


    private fun bindFunctionDeclaration(function: FunctionDeclarationSyntax) {
        val parameters = mutableListOf<ParameterSymbol>()

        val seenParameters = mutableSetOf<String>()

        for (parameterSyntax in function.parameters) {
            val name = parameterSyntax.identifier.literal
            if (!seenParameters.add(name)) {
                diagnosticsBag.reportParameterAlreadyDeclared(parameterSyntax.identifier.span(), name)
                continue
            }
            val type = bindTypeClause(parameterSyntax.typeClause)
            val parameter = ParameterSymbol(name, type)
            parameters.add(parameter)
        }

        val returnType = bindOptionalReturnTypeClause(function.returnTypeClause) ?: TypeSymbol.Unit

        if (returnType !is TypeSymbol.Unit) {
            diagnosticsBag.reportFunctionsAreNotSupported(function.span())
        }

        val functionSymbol = FunctionSymbol(function.identifier.literal, parameters, returnType, function)
        if (scope.tryDeclareFunction(functionSymbol) == null) {
            diagnosticsBag.reportFunctionAlreadyDeclared(function.identifier.span(), function.identifier.literal)
        }
    }

    private fun bindOptionalReturnTypeClause(returnTypeClause: FunctionReturnTypeSyntax?): TypeSymbol? {
        if (returnTypeClause == null) {
            return null
        }
        return bindReturnTypeClause(returnTypeClause)
    }

    private fun bindReturnTypeClause(returnTypeClause: FunctionReturnTypeSyntax): TypeSymbol {
        val type = TypeSymbol.fromName(returnTypeClause.type.literal)
        if (type == null) {
            diagnosticsBag.reportUndefinedType(returnTypeClause.type.span(), returnTypeClause.type.literal)
            return TypeSymbol.Error
        }
        return type
    }

    private val diagnosticsBag = DiagnosticsBag()

    override val diagnostics = diagnosticsBag.diagnostics

    fun bindStatement(syntax: StatementSyntax): BoundStatement {
        return when (syntax) {
            is BlockStatementSyntax -> bindBlockStatement(syntax)
            is ExpressionStatementSyntax -> bindExpressionStatement(syntax)
            is VariableDeclarationSyntax -> bindVariableDeclaration(syntax)
            is IfStatementSyntax -> bindIfStatement(syntax)
            is WhileStatementSyntax -> bindWhileStatement(syntax)
            is ForStatementSyntax -> bindForStatement(syntax)
            is BreakStatementSyntax -> bindBreakStatement(syntax)
            is ContinueStatementSyntax -> bindContinueStatement(syntax)
        }
    }

    private fun bindContinueStatement(syntax: ContinueStatementSyntax): BoundStatement {
        val isInsideLoop = isInsideLoop()
        if (!isInsideLoop) {
            diagnosticsBag.reportContinueOutsideLoop(syntax.span())
        }
        return BoundContinueStatement()
    }

    private fun bindBreakStatement(syntax: BreakStatementSyntax): BoundStatement {
        if (!isInsideLoop()) {
            diagnosticsBag.reportBreakOutsideLoop(syntax.span())
        }
        return BoundBreakStatement()
    }

    private fun isInsideLoop(): Boolean {
        var isInsideLoop = false
        for (keyword in controlStructureStack) {
            if (keyword == Keyword.While || keyword == Keyword.For) {
                isInsideLoop = true
                break
            }
        }
        return isInsideLoop
    }

    private fun bindForStatement(syntax: ForStatementSyntax): BoundStatement {
        val boundLower = bindExpression(syntax.lowerBound)
        val boundUpper = bindExpression(syntax.upperBound)

        if (boundLower.type != TypeSymbol.Int) {
            diagnosticsBag.reportCannotConvert(syntax.lowerBound.span(), TypeSymbol.Int, boundLower.type)
        }

        if (boundUpper.type != TypeSymbol.Int) {
            diagnosticsBag.reportCannotConvert(syntax.upperBound.span(), TypeSymbol.Int, boundUpper.type)
        }

        scope = BoundScope(scope)

        val name = syntax.identifier.literal
        val variable = LocalVariableSymbol(name, TypeSymbol.Int, isFinal = true)
        val declaredVariable = scope.tryLookupVariable(name)
        if (declaredVariable != null) {
            diagnosticsBag.reportVariableAlreadyDeclared(syntax.identifier.span(), name)
        }

        scope.tryDeclareVariable(variable)
        controlStructureStack.push(Keyword.For)
        val body = bindStatement(syntax.body)
        controlStructureStack.pop()
        scope = scope.parent!!
        return BoundForStatement(variable, boundLower, syntax.rangeOperator.token, boundUpper, body)
    }

    private fun bindWhileStatement(syntax: WhileStatementSyntax): BoundStatement {
        val condition = bindExpressionWithType(syntax.condition, TypeSymbol.Boolean)
        controlStructureStack.push(Keyword.While)
        val body = bindStatement(syntax.body)
        controlStructureStack.pop()
        return BoundWhileStatement(condition, body)
    }


    private fun bindIfStatement(syntax: IfStatementSyntax): BoundStatement {
        val condition = bindExpressionWithType(syntax.condition, TypeSymbol.Boolean)
        val thenStatement = bindStatement(syntax.thenStatement)
        val elseStatement = syntax.elseClause?.let { bindStatement(it.elseStatementSyntax) }
        return BoundIfStatement(condition, thenStatement, elseStatement)
    }

    private fun bindExpressionWithType(syntax: ExpressionSyntax, expectedType: TypeSymbol): BoundExpression {
        return bindConversion(syntax, expectedType)
    }

    private fun bindConversion(
        syntax: ExpressionSyntax,
        expectedType: TypeSymbol,
        isCastExplicit: Boolean = false,
    ): BoundExpression {
        val boundExpression = bindExpression(syntax)
        val textSpan = syntax.span()
        return bindConversion(boundExpression, expectedType, textSpan, isCastExplicit)
    }

    private fun bindConversion(
        boundExpression: BoundExpression,
        expectedType: TypeSymbol,
        textSpan: TextSpan,
        isCastExplicit: Boolean = false,
    ): BoundExpression {
        val conversion = Conversion.classify(boundExpression.type, expectedType)
        if (conversion.isIdentity) {
            return boundExpression
        }
        if (conversion.isNone || (conversion.isExplicit && !isCastExplicit)) {
            diagnosticsBag.reportCannotConvert(textSpan, expectedType, boundExpression.type)
            return BoundErrorExpression
        }

        return BoundConversionExpression(expectedType, boundExpression, conversion)
    }

    private fun bindVariableDeclaration(syntax: VariableDeclarationSyntax): BoundStatement {
        val initializer = bindExpression(syntax.initializer)
        val isFinal = syntax.keyword is Keyword.Val
        val typeClause = bindOptionalTypeClause(syntax.typeClauseSyntax)
        val type = typeClause ?: initializer.type
        val convertedInitializer = bindConversion(initializer, type, syntax.initializer.span())
        val symbol = if (function == null) GlobalVariableSymbol(
            syntax.identifier.literal,
            type,
            isFinal
        ) else LocalVariableSymbol(syntax.identifier.literal, type, isFinal)
        val isVariableAlreadyDeclared = scope.tryDeclareVariable(symbol) == null
        if (isVariableAlreadyDeclared) {
            diagnosticsBag.reportVariableAlreadyDeclared(syntax.identifier.span(), syntax.identifier.literal)
        }
        return BoundVariableDeclaration(symbol, convertedInitializer)
    }

    private fun bindOptionalTypeClause(syntax: TypeClauseSyntax?): TypeSymbol? {
        if (syntax == null) return null
        return bindTypeClause(syntax)
    }

    private fun bindTypeClause(syntax: TypeClauseSyntax): TypeSymbol {
        val type = TypeSymbol.fromName(syntax.type.literal)
        if (type == null) {
            diagnosticsBag.reportUndefinedType(syntax.type.span(), syntax.type.literal)
            return TypeSymbol.Error
        }
        return type
    }

    private fun bindExpressionStatement(syntax: ExpressionStatementSyntax): BoundStatement {
        val boundExpression = bindExpression(syntax.expression, canBeVoid = true)
        return BoundExpressionStatement(boundExpression)
    }

    private fun bindBlockStatement(syntax: BlockStatementSyntax): BoundStatement {
        scope = BoundScope(scope)
        val statements = syntax.statements.map {
            bindStatement(it)
        }
        scope = scope.parent!!
        return BoundBlockStatement(
            statements
        )
    }

    private fun bindExpressionInternal(syntax: ExpressionSyntax): BoundExpression {
        return when (syntax) {
            is LiteralExpressionSyntax -> {
                bindLiteralExpression(syntax)
            }

            is UnaryExpressionSyntax -> {
                bindUnaryExpression(syntax)
            }

            is BinaryExpressionSyntax -> {
                bindBinaryExpression(syntax)
            }


            is ParenthesizedExpressionSyntax -> {
                bindExpression(syntax.expressionSyntax)
            }

            is NameExpressionSyntax -> bindNameExpressionSyntax(syntax)
            is AssignmentExpressionSyntax -> bindAssignmentExpression(syntax)
            is CallExpressionSyntax -> bindCallExpression(syntax)
            is CastExpressionSyntax -> bindCastExpression(syntax)
        }
    }

    private fun bindCastExpression(syntax: CastExpressionSyntax): BoundExpression {
        val type = TypeSymbol.fromName(syntax.typeToken.literal)
        if (type == null) {
            diagnosticsBag.reportUndefinedType(syntax.typeToken.span(), syntax.typeToken.literal)
            return BoundErrorExpression
        }

        return bindConversion(syntax.expressionSyntax, type, isCastExplicit = true)

    }

    private fun bindExpression(syntax: ExpressionSyntax, canBeVoid: Boolean = false): BoundExpression {
        val result = bindExpressionInternal(syntax)
        if (!canBeVoid && result.type == TypeSymbol.Unit) {
            val span = syntax.span()
            diagnosticsBag.reportExpressionMustHaveValue(span)
            return BoundErrorExpression
        }
        return result
    }

    private fun bindCallExpression(syntax: CallExpressionSyntax): BoundExpression {
        val functionName = syntax.functionIdentifier.literal
        val declaredFunction = scope.tryLookupFunction(functionName)
        val functionSymbol =
            declaredFunction ?: ProteusExternalFunction.lookup(functionName)?.symbol
        if (functionSymbol == null) {
            diagnosticsBag.reportUndefinedFunction(syntax.functionIdentifier.span(), functionName)
            return BoundErrorExpression
        }
        if (syntax.arguments.count < functionSymbol.parameters.size) {
            diagnosticsBag.reportTooFewArguments(
                TextSpan(syntax.closeParenthesis.span().start - 1, 1),
                functionName,
                functionSymbol.parameters.size,
                syntax.arguments.count
            )
            return BoundErrorExpression
        }
        if (syntax.arguments.count > functionSymbol.parameters.size) {
            val count = syntax.arguments.count - functionSymbol.parameters.size
            val start = syntax.arguments.get(syntax.arguments.count - count).span().start
            val end = syntax.arguments.get(syntax.arguments.count - 1).span().end
            val span = TextSpan(start, end - start)
            diagnosticsBag.reportTooManyArguments(
                span,
                functionName,
                functionSymbol.parameters.size,
                syntax.arguments.count
            )
            return BoundErrorExpression
        }
        val boundParameters: MutableList<BoundExpression> = mutableListOf()
        for ((index, parameter) in functionSymbol.parameters.withIndex()) {

            val argument: ExpressionSyntax = syntax.arguments.get(index)
            val boundArgument = bindExpression(argument)
            if (boundArgument.type == TypeSymbol.Error) return BoundErrorExpression
            if (!boundArgument.type.isAssignableTo(parameter.type)) {
                diagnosticsBag.reportCannotConvert(argument.span(), parameter.type, boundArgument.type)
                return BoundErrorExpression
            }
            boundParameters.add(boundArgument)
        }


        return BoundCallExpression(functionSymbol, boundParameters, isExternal = declaredFunction == null)
    }

    private fun bindAssignmentExpression(syntax: AssignmentExpressionSyntax): BoundExpression {
        val assignmentOperator = syntax.assignmentOperator.token
        val boundExpression = bindExpression(syntax.expression)
        val variableName = syntax.identifierToken.literal
        val declaredVariable = scope.tryLookupVariable(variableName)
        if (declaredVariable == null) {
            diagnosticsBag.reportUnresolvedReference(syntax.identifierToken.span(), variableName)
            return boundExpression
        }
        if (declaredVariable.isFinal) {
            diagnosticsBag.reportFinalVariableCannotBeReassigned(syntax.identifierToken.span(), variableName)
            return BoundErrorExpression
        }
        val convertedExpression = bindConversion(boundExpression, declaredVariable.type, syntax.expression.span())
        return BoundAssignmentExpression(
            declaredVariable,
            convertedExpression,
            assignmentOperator,
            returnAssignment = true
        )
    }

    private fun bindNameExpressionSyntax(syntax: NameExpressionSyntax): BoundExpression {
        val name = syntax.identifierToken.literal
        if (name.isEmpty()) {
            return BoundErrorExpression
        }
        val variable = scope.tryLookupVariable(name)
        if (variable == null) {
            diagnosticsBag.reportUnresolvedReference(syntax.identifierToken.span(), name)
            return BoundErrorExpression
        }
        return BoundVariableExpression(variable)
    }


    private fun bindBinaryExpression(binaryExpression: BinaryExpressionSyntax): BoundExpression {

        val boundLeft = bindExpression(binaryExpression.left)
        val boundRight = bindExpression(binaryExpression.right)
        if (boundLeft.type is TypeSymbol.Error || boundRight.type is TypeSymbol.Error) {
            return BoundErrorExpression
        }
        val binaryOperator =
            BoundBinaryOperator.bind(binaryExpression.operatorToken.token, boundLeft.type, boundRight.type)
        if (binaryOperator == null) {
            diagnosticsBag.reportBinaryOperatorMismatch(
                binaryExpression.operatorToken.literal,
                binaryExpression.operatorToken.span(),
                boundLeft.type,
                boundRight.type
            )
            return BoundErrorExpression
        }
        return BoundBinaryExpression(boundLeft, boundRight, binaryOperator)

    }


    private fun bindUnaryExpression(unaryExpression: UnaryExpressionSyntax): BoundExpression {
        val boundOperand = bindExpression(unaryExpression.operand)
        if (boundOperand.type is TypeSymbol.Error) {
            return BoundErrorExpression
        }
        val boundOperator = BoundUnaryOperator.bind(unaryExpression.operatorSyntaxToken.token, boundOperand.type)
        if (boundOperator == null) {
            diagnosticsBag.reportUnaryOperatorMismatch(
                unaryExpression.operatorSyntaxToken.literal,
                unaryExpression.operatorSyntaxToken.span(),
                boundOperand.type
            )
            return BoundErrorExpression
        }
        return BoundUnaryExpression(boundOperand, boundOperator)
    }


    private fun bindLiteralExpression(syntax: LiteralExpressionSyntax): BoundLiteralExpression<*> {
        val value = evaluateValueOfLiteralExpression(syntax)
        return BoundLiteralExpression(value)
    }

    private fun evaluateValueOfLiteralExpression(syntax: LiteralExpressionSyntax): Any {
        return syntax.value
    }


}