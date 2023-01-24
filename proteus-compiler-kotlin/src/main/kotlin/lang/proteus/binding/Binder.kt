package lang.proteus.binding

import lang.proteus.diagnostics.Diagnosable
import lang.proteus.diagnostics.DiagnosticsBag
import lang.proteus.diagnostics.TextLocation
import lang.proteus.generation.Lowerer
import lang.proteus.generation.Optimizer
import lang.proteus.symbols.*
import lang.proteus.syntax.lexer.token.Keyword
import lang.proteus.syntax.parser.*
import lang.proteus.syntax.parser.statements.*
import java.util.*

internal class Binder(
    private var scope: BoundScope,
    private val function: FunctionSymbol?,
) : Diagnosable {


    init {
        if (function != null) {
            for (parameter in function.parameters) {
                scope.tryDeclareVariable(parameter, function.declaration.syntaxTree)
            }
        }
    }

    private val controlStructureStack: Stack<Keyword> = Stack()


    companion object {


        fun bindGlobalScope(previous: BoundGlobalScope?, syntax: CompilationUnitSyntax): BoundGlobalScope {
            val parentScope = createParentScopes(previous)
            val binder = Binder(parentScope ?: BoundScope(null), null)
            val diagnostics = DiagnosticsBag()
            val importGraph = ImportGraph.create(syntax.syntaxTree)
            val cycles = importGraph.findCycles()
            for (cycle in cycles) {
                val location = syntax.members.first().location
                diagnostics.reportCircularDependency(location, cycle)
            }
            val trees = importGraph.getTrees()
            for (tree in trees) {
                diagnostics.addAll(tree.diagnostics)
            }
            val symbolMap: MutableMap<SyntaxTree, MutableSet<Symbol>> = mutableMapOf()
            for (tree in trees) {
                val symbols = mutableSetOf<Symbol>()
                symbols.addAll(importGraph.gatherExportedSymbols(binder, tree))
                symbols.addAll(importGraph.gatherImportedSymbols(binder, tree))
                symbolMap[tree] = symbols
            }

            for ((tree, symbols) in symbolMap) {
                for (symbol in symbols) {
                    if (symbol is FunctionSymbol) {
                        binder.scope.tryDeclareFunction(symbol, tree)
                    } else if (symbol is VariableSymbol) {
                        binder.scope.tryDeclareVariable(symbol, tree)
                    }
                }
            }
            diagnostics.addAll(binder.diagnostics)
            val variables = binder.scope.getDeclaredVariables()
            val functions = binder.scope.getDeclaredFunctions()
            if (previous != null) {
                diagnostics.addAll(previous.diagnostics)
            }
            return BoundGlobalScope(previous, diagnostics.diagnostics, functions, variables)
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
                for ((syntaxTree, variables) in previous.variables) {
                    for (variable in variables)
                        scope.tryDeclareVariable(variable, syntaxTree)
                }
                for ((syntaxTree, functions) in previous.functions) {
                    for (function in functions)
                        scope.tryDeclareFunction(function, syntaxTree)
                }

                parent = scope
            }
            return parent;
        }

        fun bindProgram(
            globalScope: BoundGlobalScope,
            mainTree: SyntaxTree,
            optimize: Boolean = true,
        ): BoundProgram {

            val parentScope = createParentScopes(globalScope)

            val functionBodies = mutableMapOf<FunctionSymbol, BoundBlockStatement>()

            val diagnostics = DiagnosticsBag()
            diagnostics.addAll(globalScope.diagnostics)


            for ((syntaxTree, functions) in globalScope.functions) {
                for (function in functions) {
                    val binder = Binder(parentScope ?: BoundScope(null), function)
                    val functionBody = function.declaration.body ?: continue
                    val body = binder.bindStatement(functionBody)
                    if (!binder.hasErrors()) {
                        val loweredBody = Lowerer.lower(body)
                        val optimizedBody = if (optimize) Optimizer.optimize(loweredBody) else loweredBody
                        val graph = ControlFlowGraph.createAndOutput(optimizedBody)
                        if (!graph.allPathsReturn()) {
                            diagnostics.reportAllCodePathsMustReturn(function.declaration.identifier.location)
                        } else {
                            if (function.returnType !is TypeSymbol.Unit) {
                                for (incoming in graph.end.incoming) {
                                    if (incoming.statements.lastOrNull() !is BoundReturnStatement) {
                                        diagnostics.reportAllCodePathsMustReturn(function.declaration.identifier.location)
                                    }
                                }
                            }
                        }
                        for (block in graph.blocks) {
                            if (block.isEnd != true && block.isStart != true && block.incoming.size == 0) {
                                // todo: better error message
                                diagnostics.reportUnreachableCode(function.declaration.identifier.location);
                            }
                        }
                        functionBodies[function] = optimizedBody
                    }
                    diagnostics.addAll(binder.diagnostics)
                }
            }
            val mainFunction = validateMainFunction(globalScope.functions[mainTree]!!, diagnostics)


            return BoundProgram(globalScope, diagnostics.diagnostics, functionBodies, mainFunction)
        }

        private fun validateMainFunction(functions: Set<FunctionSymbol>, diagnostics: DiagnosticsBag): FunctionSymbol {
            val mainFunction = functions.firstOrNull { it.name == "main" }
            if (mainFunction == null) {
                throw IllegalStateException("No entry point found. Specify a main function.")
            } else if (mainFunction.parameters.isNotEmpty()) {
                diagnostics.reportMainMustHaveNoParameters(mainFunction)
            } else if (mainFunction.returnType != TypeSymbol.Unit) {
                diagnostics.invalidMainFunctionReturnType(mainFunction, TypeSymbol.Unit)
            }
            return mainFunction
        }
    }


    internal fun bindFunctionDeclaration(
        function: FunctionDeclarationSyntax,
        tree: SyntaxTree,
        defineSymbol: Boolean = true,
    ): FunctionSymbol {
        val parameters = mutableListOf<ParameterSymbol>()

        val seenParameters = mutableSetOf<String>()

        for (parameterSyntax in function.parameters) {
            val name = parameterSyntax.identifier.literal
            if (!seenParameters.add(name)) {
                diagnosticsBag.reportParameterAlreadyDeclared(parameterSyntax.identifier.location, name)
                continue
            }
            val type = bindTypeClause(parameterSyntax.typeClause)
            val parameter = ParameterSymbol(name, type)
            parameters.add(parameter)
        }

        val returnType = bindOptionalReturnTypeClause(function.returnTypeClause) ?: TypeSymbol.Unit
        val functionSymbol = FunctionSymbol(function.identifier.literal, parameters, returnType, function)
        if (defineSymbol) {
            if (scope.tryDeclareFunction(functionSymbol, tree) == null) {
                diagnosticsBag.reportFunctionAlreadyDeclared(function.identifier.location, function.identifier.literal)
            }
        }
        return functionSymbol
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
            diagnosticsBag.reportUndefinedType(returnTypeClause.type.location, returnTypeClause.type.literal)
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
            is ReturnStatementSyntax -> bindReturnStatement(syntax)
        }
    }

    private fun bindReturnStatement(syntax: ReturnStatementSyntax): BoundStatement {
        val statement = if (syntax.expression == null) null else bindExpression(syntax.expression)
        if (!isInsideFunction()) {
            diagnosticsBag.reportReturnNotAllowed(syntax.returnKeyword.location)
        } else {
            val functionReturnType = function!!.returnType
            val actualReturnType = statement?.type ?: TypeSymbol.Unit
            val conversion = Conversion.classify(actualReturnType, functionReturnType)
            if (conversion.isNone) {
                diagnosticsBag.reportInvalidReturnType(
                    syntax.expression?.location ?: syntax.returnKeyword.location,
                    functionReturnType,
                    actualReturnType
                )
            }
        }
        return BoundReturnStatement(statement)
    }

    private fun isInsideFunction(): Boolean {
        return function != null
    }

    private fun bindContinueStatement(syntax: ContinueStatementSyntax): BoundStatement {
        val isInsideLoop = isInsideLoop()
        if (!isInsideLoop) {
            diagnosticsBag.reportContinueOutsideLoop(syntax.location)
        }
        return BoundContinueStatement()
    }

    private fun bindBreakStatement(syntax: BreakStatementSyntax): BoundStatement {
        if (!isInsideLoop()) {
            diagnosticsBag.reportBreakOutsideLoop(syntax.location)
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
            diagnosticsBag.reportCannotConvert(syntax.lowerBound.location, TypeSymbol.Int, boundLower.type)
        }

        if (boundUpper.type != TypeSymbol.Int) {
            diagnosticsBag.reportCannotConvert(syntax.upperBound.location, TypeSymbol.Int, boundUpper.type)
        }

        scope = BoundScope(scope)

        val name = syntax.identifier.literal
        val variable = LocalVariableSymbol(name, TypeSymbol.Int, isFinal = true)
        val declaredVariable = scope.tryLookupVariable(name, syntax.syntaxTree)
        if (declaredVariable != null) {
            diagnosticsBag.reportVariableAlreadyDeclared(syntax.identifier.location, name)
        }

        scope.tryDeclareVariable(variable, syntax.syntaxTree)
        controlStructureStack.push(Keyword.For)
        val body = bindStatement(syntax.body)
        controlStructureStack.pop()
        scope = scope.parent!!
        return BoundForStatement(variable, boundLower, syntax.rangeOperator.token, boundUpper, body)
    }

    private fun bindWhileStatement(syntax: WhileStatementSyntax): BoundStatement {
        val condition = bindExpressionWithType(syntax.condition, TypeSymbol.Boolean)
        controlStructureStack.push(Keyword.While)
        scope = BoundScope(scope)
        val body = bindStatement(syntax.body)
        scope = scope.parent!!
        controlStructureStack.pop()
        return BoundWhileStatement(condition, body)
    }


    private fun bindIfStatement(syntax: IfStatementSyntax): BoundStatement {
        val condition = bindExpressionWithType(syntax.condition, TypeSymbol.Boolean)
        scope = BoundScope(scope)
        val thenStatement = bindStatement(syntax.thenStatement)
        scope = scope.parent!!
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
        val textSpan = syntax.location
        return bindConversion(boundExpression, expectedType, textSpan, isCastExplicit)
    }

    private fun bindConversion(
        boundExpression: BoundExpression,
        expectedType: TypeSymbol,
        textLocation: TextLocation,
        isCastExplicit: Boolean = false,
    ): BoundExpression {
        val conversion = Conversion.classify(boundExpression.type, expectedType)
        if (conversion.isIdentity) {
            return boundExpression
        }
        if (conversion.isNone || (conversion.isExplicit && !isCastExplicit)) {
            diagnosticsBag.reportCannotConvert(textLocation, expectedType, boundExpression.type)
            return BoundErrorExpression
        }

        return BoundConversionExpression(expectedType, boundExpression, conversion)
    }

    internal fun bindVariableDeclaration(syntax: VariableDeclarationSyntax, defineSymbol: Boolean = true): BoundVariableDeclaration {
        val initializer = bindExpression(syntax.initializer)
        val isConst = syntax.keyword is Keyword.Const
        val isFinal = isConst || syntax.keyword is Keyword.Val
        val typeClause = bindOptionalTypeClause(syntax.typeClauseSyntax)
        val type = typeClause ?: initializer.type
        val convertedInitializer = bindConversion(initializer, type, syntax.initializer.location)
        val symbol = if (function == null) GlobalVariableSymbol(
            syntax.identifier.literal,
            type,
            isFinal,
        ) else LocalVariableSymbol(syntax.identifier.literal, type, isFinal)
        if (isConst) {
            if (isExpressionConst(convertedInitializer)) {
                symbol.constantValue = convertedInitializer
            }
            if (symbol.constantValue == null) {
                diagnosticsBag.reportExpectedConstantExpression(syntax.initializer.location)
            }
        }
        if(defineSymbol) {
            val isVariableAlreadyDeclared = scope.tryDeclareVariable(symbol, syntax.syntaxTree) == null
            if (isVariableAlreadyDeclared) {
                diagnosticsBag.reportVariableAlreadyDeclared(syntax.identifier.location, syntax.identifier.literal)
            }
        }
        return BoundVariableDeclaration(symbol, convertedInitializer)
    }

    private fun isExpressionConst(expression: BoundExpression): Boolean {
        return when (expression) {
            is BoundLiteralExpression<*> -> true
            is BoundVariableExpression -> expression.variable.isConst
            is BoundUnaryExpression -> isExpressionConst(expression.operand)
            is BoundBinaryExpression -> isExpressionConst(expression.left) && isExpressionConst(expression.right)
            else -> false
        }
    }

    private fun bindOptionalTypeClause(syntax: TypeClauseSyntax?): TypeSymbol? {
        if (syntax == null) return null
        return bindTypeClause(syntax)
    }

    private fun bindTypeClause(syntax: TypeClauseSyntax): TypeSymbol {
        val type = TypeSymbol.fromName(syntax.type.literal)
        if (type == null) {
            diagnosticsBag.reportUndefinedType(syntax.type.location, syntax.type.literal)
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
            diagnosticsBag.reportUndefinedType(syntax.typeToken.location, syntax.typeToken.literal)
            return BoundErrorExpression
        }

        return bindConversion(syntax.expressionSyntax, type, isCastExplicit = true)

    }

    private fun bindExpression(syntax: ExpressionSyntax, canBeVoid: Boolean = false): BoundExpression {
        val result = bindExpressionInternal(syntax)
        if (!canBeVoid && result.type == TypeSymbol.Unit) {
            val span = syntax.location
            diagnosticsBag.reportExpressionMustHaveValue(span)
            return BoundErrorExpression
        }
        return result
    }

    private fun bindCallExpression(syntax: CallExpressionSyntax): BoundExpression {
        val functionName = syntax.functionIdentifier.literal
        val declaredFunction = scope.tryLookupFunction(functionName, syntax.syntaxTree)
        if (declaredFunction == null) {
            diagnosticsBag.reportUndefinedFunction(syntax.functionIdentifier.location, functionName)
            return BoundErrorExpression
        }
        if (declaredFunction.declaration.isExternal) {
            val externalFunction = ProteusExternalFunction.lookup(declaredFunction.declaration)
            if (externalFunction == null) {
                diagnosticsBag.reportExternalFunctionNotFound(declaredFunction.declaration, syntax.location)
                return BoundErrorExpression
            }
        }
        if (syntax.arguments.count < declaredFunction.parameters.size) {
            val location = syntax.closeParenthesis.location.copy(span = syntax.functionIdentifier.span())
            diagnosticsBag.reportTooFewArguments(
                location,
                functionName,
                declaredFunction.parameters.size,
                syntax.arguments.count
            )
            return BoundErrorExpression
        }
        if (syntax.arguments.count > declaredFunction.parameters.size) {
            val count = syntax.arguments.count - declaredFunction.parameters.size
            val location = syntax.arguments.get(syntax.arguments.count - count).location
            diagnosticsBag.reportTooManyArguments(
                location,
                functionName,
                declaredFunction.parameters.size,
                syntax.arguments.count
            )
            return BoundErrorExpression
        }
        val boundParameters: MutableList<BoundExpression> = mutableListOf()
        for ((index, parameter) in declaredFunction.parameters.withIndex()) {

            val argument: ExpressionSyntax = syntax.arguments.get(index)
            val boundArgument = bindExpression(argument)
            if (boundArgument.type == TypeSymbol.Error) return BoundErrorExpression
            if (!boundArgument.type.isAssignableTo(parameter.type)) {
                diagnosticsBag.reportCannotConvert(argument.location, parameter.type, boundArgument.type)
                return BoundErrorExpression
            }
            boundParameters.add(boundArgument)
        }


        return BoundCallExpression(declaredFunction, boundParameters)
    }

    private fun bindAssignmentExpression(syntax: AssignmentExpressionSyntax): BoundExpression {
        val assignmentOperator = syntax.assignmentOperator.token
        val boundExpression = bindExpression(syntax.expression)
        val variableName = syntax.identifierToken.literal
        val declaredVariable = scope.tryLookupVariable(variableName, syntax.syntaxTree)
        if (declaredVariable == null) {
            diagnosticsBag.reportUnresolvedReference(syntax.identifierToken.location, variableName)
            return boundExpression
        }
        if (declaredVariable.isReadOnly) {
            diagnosticsBag.reportFinalVariableCannotBeReassigned(syntax.identifierToken.location, declaredVariable)
            return BoundErrorExpression
        }
        val convertedExpression = bindConversion(boundExpression, declaredVariable.type, syntax.expression.location)
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
        val variable = scope.tryLookupVariable(name, syntax.syntaxTree)
        if (variable == null) {
            diagnosticsBag.reportUnresolvedReference(syntax.identifierToken.location, name)
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
                binaryExpression.operatorToken.location,
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
                unaryExpression.operatorSyntaxToken.location,
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