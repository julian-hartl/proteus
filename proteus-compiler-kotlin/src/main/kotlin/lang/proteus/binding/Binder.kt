package lang.proteus.binding

import lang.proteus.diagnostics.Diagnosable
import lang.proteus.diagnostics.DiagnosticsBag
import lang.proteus.diagnostics.TextLocation
import lang.proteus.generation.Lowerer
import lang.proteus.generation.optimization.CodeOptimizer
import lang.proteus.symbols.*
import lang.proteus.syntax.lexer.token.Keyword
import lang.proteus.syntax.parser.*
import lang.proteus.syntax.parser.statements.*
import java.util.*

internal class Binder(
    private var scope: BoundScope,
    private val function: FunctionSymbol?,
    private val structMembers: Map<StructSymbol, Set<StructMemberSymbol>>?,
) : Diagnosable {

    public val boundScope get() = scope


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
            val binder = Binder(parentScope ?: BoundScope(null), null, null)
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
                symbols.addAll(importGraph.gatherExportedSymbols(tree))
                symbols.addAll(importGraph.gatherImportedSymbols(tree))
                symbolMap[tree] = symbols
            }
            diagnostics.concat(importGraph.diagnosticsBag)

            for ((tree, symbols) in symbolMap) {
                for (symbol in symbols) {
                    when (symbol) {
                        is FunctionSymbol -> {
                            binder.scope.tryDeclareFunction(symbol, tree)
                        }

                        is VariableSymbol -> {
                            binder.scope.tryDeclareVariable(symbol, tree)
                        }

                        is StructSymbol -> {
                            binder.scope.tryDeclareStruct(symbol, tree)
                        }

                        else -> {
                            throw Exception("Unexpected symbol type: ${symbol::class.simpleName}")
                        }
                    }
                }
            }
            diagnostics.addAll(binder.diagnostics)
            val variables = binder.scope.getDeclaredVariables()
            val functions = binder.scope.getDeclaredFunctions()
            val structs = binder.scope.getDeclaredStructs()
            if (previous != null) {
                diagnostics.addAll(previous.diagnostics)
            }
            return BoundGlobalScope(previous, diagnostics.diagnostics, functions, variables, structs)
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
                for ((syntaxTree, variables) in previous.mappedVariables) {
                    for (variable in variables)
                        scope.tryDeclareVariable(variable, syntaxTree)
                }
                for ((syntaxTree, functions) in previous.mappedFunctions) {
                    for (function in functions)
                        scope.tryDeclareFunction(function, syntaxTree)
                }

                for ((syntaxTree, structs) in previous.mappedStructs) {
                    for (struct in structs)
                        scope.tryDeclareStruct(struct, syntaxTree)
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
            val structMembers = mutableMapOf<StructSymbol, Set<StructMemberSymbol>>()
            for (struct in globalScope.structs) {
                val binder = Binder(parentScope ?: BoundScope(null), null, null)
                structMembers[struct] = binder.bindStructMembers(struct.declaration.members, mainTree).toSet()
            }

            val functionBodies = mutableMapOf<FunctionSymbol, BoundBlockStatement>()

            val diagnostics = DiagnosticsBag()
            diagnostics.addAll(globalScope.diagnostics)


            for (function in globalScope.functions) {
                val binder = Binder(BoundScope(parentScope), function, structMembers)
                val functionBody = function.declaration.body ?: continue
                val body = binder.bindBlockStatement(functionBody)
                var optimizedBody = BoundBlockStatement(listOf())
                if (!binder.hasErrors()) {
                    optimizedBody = if (optimize) CodeOptimizer.optimize(body) else body
                    optimizedBody = Lowerer.lower(optimizedBody)
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
                            diagnostics.reportUnreachableCode(function.declaration.body.location);
                        }
                    }
                }
                functionBodies[function] = optimizedBody
                diagnostics.addAll(binder.diagnostics)
            }

            val variableInitializers = mutableMapOf<GlobalVariableSymbol, BoundExpression>()
            for (variable in globalScope.variables) {
                if (variable is GlobalVariableSymbol) {
                    val binder = Binder(parentScope ?: BoundScope(null), null, structMembers)
                    val initializer = variable.declarationSyntax.initializer
                    val boundInitializer = binder.bindExpression(initializer)
                    val optimized = CodeOptimizer.optimize(boundInitializer)
                    variableInitializers[variable] = optimized
                    diagnostics.addAll(binder.diagnostics)
                }
            }


            val mainFunction = validateMainFunction(globalScope.mappedFunctions[mainTree] ?: emptySet(), diagnostics)


            return BoundProgram(
                globalScope,
                diagnostics.diagnostics,
                functionBodies,
                variableInitializers,
                structMembers,
                mainFunction
            )
        }

        private fun validateMainFunction(functions: Set<FunctionSymbol>, diagnostics: DiagnosticsBag): FunctionSymbol {
            val mainFunction = functions.firstOrNull { it.simpleName == "main" }
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

    internal fun bindStructDeclaration(
        structDeclaration: StructDeclarationSyntax,
        tree: SyntaxTree,
    ): StructSymbol {
        val name = structDeclaration.identifier.literal
        val structSymbol = StructSymbol(structDeclaration, name, tree)
        if (scope.tryDeclareStruct(structSymbol, tree) == null) {
            diagnosticsBag.reportStructAlreadyDeclared(structDeclaration.identifier.location, name)
        }
        return structSymbol
    }

    internal fun bindStructMembers(
        members: List<StructMemberSyntax>,
        tree: SyntaxTree,
    ): List<StructMemberSymbol> {
        val seenMembers = mutableSetOf<String>()
        val result = mutableListOf<StructMemberSymbol>()
        for (member in members) {
            val name = member.identifier.literal
            if (!seenMembers.add(name)) {
                diagnosticsBag.reportStructMemberAlreadyDeclared(member.identifier.location, name)
                continue
            }
            val type = bindTypeClause(member.type)
            val symbol = StructMemberSymbol(name, type, member, tree)
            result.add(symbol)
        }
        return result
    }

    private data class ParameterInfo(
        val name: String,
        val isMutable: Boolean,
        val type: TypeSymbol,
    )

    internal fun bindFunctionDeclaration(
        function: FunctionDeclarationSyntax,
        tree: SyntaxTree,
    ): FunctionSymbol {
        val parameterInfos = mutableListOf<ParameterInfo>()

        val seenParameters = mutableSetOf<String>()

        for (parameterSyntax in function.parameters) {
            val name = parameterSyntax.identifier.literal
            if (!seenParameters.add(name)) {
                diagnosticsBag.reportParameterAlreadyDeclared(parameterSyntax.identifier.location, name)
                continue
            }
            val type = bindTypeClause(parameterSyntax.typeClause)
            val parameter =
                ParameterInfo(name, parameterSyntax.mutabilityToken != null, type)
            parameterInfos.add(parameter)
        }

        val returnType = bindOptionalReturnTypeClause(function.returnTypeClause) ?: TypeSymbol.Unit
        val functionSymbol = FunctionSymbol(null, returnType, function, tree)
        val parameters = parameterInfos.map { ParameterSymbol(it.name, it.isMutable, it.type, tree, functionSymbol) }
        functionSymbol.parameters = parameters
        if (scope.tryDeclareFunction(functionSymbol, tree) == null) {
            diagnosticsBag.reportFunctionAlreadyDeclared(function.identifier.location, function.identifier.literal)
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
        return bindTypeLiteral(returnTypeClause.type, returnTypeClause.location)
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
        val actualReturnType = statement?.type ?: TypeSymbol.Unit
        if (!isInsideFunction()) {
            diagnosticsBag.reportReturnNotAllowed(syntax.returnKeyword.location)
        } else {
            val functionReturnType = function!!.returnType
            val conversion = Conversion.classify(actualReturnType, functionReturnType)
            if (conversion.isNone) {
                diagnosticsBag.reportInvalidReturnType(
                    syntax.expression?.location ?: syntax.returnKeyword.location,
                    functionReturnType,
                    actualReturnType
                )
            }
        }
        return BoundReturnStatement(statement, actualReturnType)
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
        val variable = LocalVariableSymbol(
            name, TypeSymbol.Int, isMut = true, syntaxTree = syntax.syntaxTree,
            enclosingFunction = this.function!!
        )
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
        reportDiagnostics: Boolean = true,
    ): BoundExpression {
        val type = boundExpression.type
        val conversion = Conversion.classify(type, expectedType)
        if (conversion.isIdentity) {
            return boundExpression
        }
        if (conversion.isNone || (conversion.isExplicit && !isCastExplicit)) {
            val hint = run {
                if (Conversion.classify(type.deref(), expectedType.deref()).isImplicit) {
                    "You need to pass by reference instead of by value. Use & to get a reference to the value."
                } else {
                    if (type is TypeSymbol.Pointer && expectedType is TypeSymbol.Pointer) {
                        if (expectedType.isMutable && !type.isMutable) {
                            return@run "You need to pass a mutable reference instead of an immutable one. Use &mut to get a mutable reference to the value."
                        } else{
                            return@run "You need to pass an immutable reference instead of a mutable one. Use & to get an immutable reference to the value."
                        }
                    }
                    null
                }
            }
            if (reportDiagnostics) {
                diagnosticsBag.reportCannotConvert(textLocation, expectedType, type, hint)
            }
            return BoundErrorExpression
        }
        return BoundConversionExpression(expectedType, boundExpression, conversion)
    }

    internal fun bindVariableDeclaration(
        syntax: VariableDeclarationSyntax,
    ): BoundVariableDeclaration {
        val initializer = bindExpression(syntax.initializer)
        val isConst = syntax.keyword is Keyword.Const
        val isMutable = syntax.mutabilityToken != null
        val typeClause = bindOptionalTypeClause(syntax.typeClauseSyntax)
        val type = (typeClause ?: initializer.type)
        val convertedInitializer = bindConversion(initializer, type, syntax.initializer.location)
        val symbol = if (function == null) GlobalVariableSymbol(
            syntax.identifier.literal,
            type,
            isMutable,
            syntaxTree = syntax.syntaxTree,
            declarationSyntax = syntax
        ) else LocalVariableSymbol(
            syntax.identifier.literal,
            type,
            isMutable,
            syntaxTree = syntax.syntaxTree,
            enclosingFunction = function
        )
        if (isConst) {
            if (isExpressionConst(convertedInitializer)) {
                symbol.constantValue = convertedInitializer
            }
            if (symbol.constantValue == null) {
                diagnosticsBag.reportExpectedConstantExpression(syntax.initializer.location)
            }
        }
        val isVariableAlreadyDeclared = scope.tryDeclareVariable(symbol, syntax.syntaxTree) == null
        if (isVariableAlreadyDeclared) {
            diagnosticsBag.reportVariableAlreadyDeclared(syntax.identifier.location, syntax.identifier.literal)
        }
        return BoundVariableDeclaration(symbol, convertedInitializer)
    }

    private fun isExpressionConst(expression: BoundExpression): Boolean {
        return when (expression) {
            is BoundLiteralExpression<*> -> true
            is BoundVariableExpression -> expression.variable.isConst
            is BoundUnaryExpression -> isExpressionConst(expression.operand)
            is BoundBinaryExpression -> isExpressionConst(expression.left) && isExpressionConst(expression.right)
            is BoundConversionExpression -> isExpressionConst(expression.expression)
            else -> false
        }
    }

    private fun bindOptionalTypeClause(syntax: TypeClauseSyntax?): TypeSymbol? {
        if (syntax == null) return null
        return bindTypeClause(syntax)
    }

    private fun bindTypeClause(syntax: TypeClauseSyntax): TypeSymbol {
        return bindTypeLiteral(syntax.type, syntax.location)
    }

    private fun bindExpressionStatement(syntax: ExpressionStatementSyntax): BoundStatement {
        val boundExpression = bindExpression(syntax.expression, canBeVoid = true)
        return BoundExpressionStatement(boundExpression)
    }

    private fun bindBlockStatement(syntax: BlockStatementSyntax): BoundBlockStatement {
        scope = BoundScope(scope)
        val statements = syntax.statements.map {
            val statement = bindStatement(it)
            if (statement is BoundExpressionStatement) {
                val expression = statement.expression
                if (expression !is BoundCallExpression && expression !is BoundAssignmentExpression && expression !is BoundErrorExpression) {
                    diagnosticsBag.reportUnusedExpression(it.location)
                }
            }
            statement
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
            is StructInitializationExpressionSyntax -> bindStructInitializationExpression(syntax)
            is MemberAccessExpressionSyntax -> bindMemberAccessExpression(syntax)
            is ReferenceExpressionSyntax -> bindReferenceExpression(syntax)
        }
    }

    private fun bindReferenceExpression(syntax: ReferenceExpressionSyntax): BoundExpression {
        val expression = bindExpression(syntax.expression)
        val isMutable = syntax.mutabilityToken != null
        if (isMutable && !isExpressionMutable(expression)) {
            diagnosticsBag.reportCannotGetMutableReferenceToImmutableValue(syntax.mutabilityToken!!.location , expression.type)
        }
        return BoundReferenceExpression(expression, isMutable = isMutable)
    }

    private fun isExpressionMutable(expression: BoundExpression): Boolean {
        return when (expression) {
            is BoundReferenceExpression -> expression.isMutable
            is BoundVariableExpression -> expression.variable.isMutable
            is BoundMemberAccessExpression -> isExpressionMutable(expression.expression)
            is BoundConversionExpression -> isExpressionMutable(expression.expression)
            is BoundLiteralExpression<*> -> true
            else -> false
        }
    }

    private fun bindMemberAccessExpression(syntax: MemberAccessExpressionSyntax): BoundExpression {
        val expression = bindExpression(syntax.expression)
        val memberName = syntax.identifier.literal
        val member = lookUpMember(expression.type, memberName, syntax.syntaxTree)
        if (member == null) {
            diagnosticsBag.reportUndefinedMember(
                syntax.identifier.location,
                memberName,
                expression.type,
                couldAssignWhenDereferenced = lookUpMember(
                    expression.type.deref(),
                    memberName,
                    syntax.syntaxTree
                ) != null
            )
            return BoundErrorExpression
        }
        return BoundMemberAccessExpression(expression, memberName, member.type)
    }

    private fun lookUpMember(type: TypeSymbol, memberName: String, tree: SyntaxTree): StructMemberSymbol? {
        val member = run {
            if (type is TypeSymbol.Struct) {
                val struct = scope.tryLookupStruct(type.name, tree)
                if (struct != null) {
                    val members = structMembers?.get(struct) ?: return@run null
                    for (member in members) {
                        if (member.name == memberName) {
                            return@run member
                        }
                    }
                    for (base in members) {
                        val member = lookUpMember(base.type, memberName, tree)
                        if (member != null) {
                            return@run member
                        }
                    }
                }

            }
            return@run null;
        }
        return member
    }

    private fun bindStructInitializationExpression(syntax: StructInitializationExpressionSyntax): BoundExpression {
        val structName = syntax.identifier.literal
        val structSymbol = scope.tryLookupStruct(structName, syntax.syntaxTree)
        if (structSymbol == null) {
            diagnosticsBag.reportUndefinedStruct(syntax.identifier.location, structName)
            return BoundErrorExpression
        }
        val arguments = bindStructMemberExpression(structSymbol, syntax.members)
        return BoundStructInitializationExpression(structSymbol, arguments)
    }

    private fun bindStructMemberExpression(
        struct: StructSymbol,
        members: SeparatedSyntaxList<StructMemberInitializationSyntax>,
    ): List<BoundStructMemberInitializationExpression> {
        val structMembers = structMembers?.get(struct)
        val membersMap = structMembers?.associateBy { it.name }
            ?: throw IllegalStateException("Struct members not bound")
        val arguments = mutableListOf<BoundStructMemberInitializationExpression>()
        val definedMembers = mutableSetOf<String>()
        for (member in members) {
            val memberName = member.identifier.literal
            val memberSymbol = membersMap[memberName]
            if (memberSymbol == null) {
                diagnosticsBag.reportUndefinedStructMember(member.identifier.location, memberName, struct.name)
                continue
            }
            val boundExpression = bindExpression(member.expression)
            val convertedExpression = bindConversion(boundExpression, memberSymbol.type, member.expression.location)
            arguments.add(BoundStructMemberInitializationExpression(memberSymbol.name, convertedExpression))
            if (definedMembers.contains(memberName)) {
                diagnosticsBag.reportStructMemberAlreadyInitialized(member.identifier.location, memberName, struct.name)
            }
            definedMembers.add(memberName)
        }
        for (member in structMembers) {
            if (!definedMembers.contains(member.name)) {
                diagnosticsBag.reportStructMemberNotInitialized(member.syntax.location, member.name, struct.name)
            }
        }
        return arguments

    }

    private fun bindCastExpression(syntax: CastExpressionSyntax): BoundExpression {
        val type = bindTypeLiteral(syntax.typeSyntax, syntax.location)
        if (type == TypeSymbol.Error) {
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
            val boundConversion = bindConversion(boundArgument, parameter.type, argument.location)
            if (boundConversion.type == TypeSymbol.Error) return BoundErrorExpression
            boundParameters.add(boundConversion)
        }


        return BoundCallExpression(declaredFunction, boundParameters)
    }

    private fun bindAssignmentExpression(syntax: AssignmentExpressionSyntax): BoundExpression {
        val assignmentOperator = syntax.assignmentOperator.token
        val assignee = bindExpression(syntax.assigneeExpression)
        val boundExpression = bindExpression(syntax.expression)
        if (assignee is BoundErrorExpression || boundExpression is BoundErrorExpression) {
            return BoundErrorExpression
        }
        val boundAssignee = try {
            BoundAssignee.fromExpression(assignee)
        } catch (e: Exception) {
            diagnosticsBag.reportInvalidAssignmentTarget(syntax.assigneeExpression.location)
            return BoundErrorExpression
        }

        if (checkAssignmentTarget(boundAssignee, syntax)) return BoundErrorExpression

        val convertedExpression =
            bindConversion(boundExpression, boundAssignee.expression.type, syntax.expression.location)
        return BoundAssignmentExpression(
            boundAssignee,
            convertedExpression,
            assignmentOperator,
            returnAssignment = true
        )
    }

    private fun checkAssignmentTarget(
        boundAssignee: BoundAssignee<out BoundExpression>,
        syntax: AssignmentExpressionSyntax,
    ): Boolean {
        when (boundAssignee) {
            is BoundAssignee.BoundMemberAssignee -> {
                val parent = boundAssignee.expression.expression.type as? TypeSymbol.Struct
                val structSymbol = structMembers?.keys?.firstOrNull { it.name == parent?.name }
                val member =
                    structMembers?.get(structSymbol)?.firstOrNull { it.name == boundAssignee.expression.memberName }
                        ?: return false
                if (!member.isMutable) {
                    diagnosticsBag.reportMemberOfStructNotMutable(
                        syntax.assigneeExpression.location,
                        member.name,
                        structSymbol!!.name
                    )
                    return true
                }
            }

            is BoundAssignee.BoundVariableAssignee -> {
                val variable = boundAssignee.variable
                if (variable.isReadOnly) {
                    if(variable is ParameterSymbol){
                        diagnosticsBag.reportImmutableParameterCannotBeReassigned(syntax.assigneeExpression.location, variable)
                    }else {
                        diagnosticsBag.reportFinalVariableCannotBeReassigned(
                            syntax.assigneeExpression.location,
                            variable
                        )
                    }
                    return true
                }
            }

            is BoundAssignee.BoundDereferenceAssignee -> {

                val expression = boundAssignee.expression
                val type = expression.operand.type as TypeSymbol.Pointer
                if (!type.isMutable) {
                    diagnosticsBag.reportCannotAssignToImmutablePointer(syntax.assigneeExpression.location, type)
                    return true
                }


            }
        }
        return false
    }

    private fun bindNameExpressionSyntax(syntax: NameExpressionSyntax): BoundExpression {
        val name = syntax.identifierToken.literal
        if (name.isEmpty()) {
            diagnosticsBag.reportInvalidName(syntax.identifierToken.location)
            return BoundErrorExpression
        }
        val symbol = scope.tryLookup(name, syntax.syntaxTree)
        if (symbol == null) {
            diagnosticsBag.reportUnresolvedReference(syntax.identifierToken.location, name)
            return BoundErrorExpression
        }
        return when (symbol) {
            is VariableSymbol -> BoundVariableExpression(symbol)
            is FunctionSymbol -> {
                diagnosticsBag.reportCannotReferenceFunction(syntax.identifierToken.location, name)
                BoundErrorExpression
            }

            is StructMemberSymbol -> {
                diagnosticsBag.reportCannotReferenceStructMember(syntax.identifierToken.location, name)
                BoundErrorExpression
            }

            is StructSymbol, is TypeSymbol -> {
                BoundTypeExpression(symbol)
            }
        }
    }


    private fun bindBinaryExpression(binaryExpression: BinaryExpressionSyntax): BoundExpression {

        val boundLeft = bindExpression(binaryExpression.left)
        var boundRight = bindExpression(binaryExpression.right)

        if (boundLeft.type is TypeSymbol.Error || boundRight.type is TypeSymbol.Error) {
            return BoundErrorExpression
        }
        /*
        Why is this needed?
         Because there are certain operators that allow two types that are not compatible to be used together
         For example, the 'is' operator allows any value to be compared to a type
         But you cannot convert a value to a type
         Solution: First check if there is a binary operator that accepts the two types
         If there is not, then try to convert the right side to the type of the left side
         If that fails, then there is no binary operator that accepts the two types
         */
        var binaryOperator =
            BoundBinaryOperator.bind(binaryExpression.operatorToken.token, boundLeft.type, boundRight.type)
        if (binaryOperator == null) {
            val originalRightType = boundRight.type
            boundRight =
                bindConversion(boundRight, boundLeft.type, binaryExpression.right.location, reportDiagnostics = false)
            binaryOperator =
                BoundBinaryOperator.bind(binaryExpression.operatorToken.token, boundLeft.type, boundRight.type)
            if (binaryOperator == null) {
                diagnosticsBag.reportBinaryOperatorMismatch(
                    binaryExpression.operatorToken.literal,
                    binaryExpression.operatorToken.location,
                    boundLeft.type,
                    originalRightType
                )
                return BoundErrorExpression
            }
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


    private fun bindTypeLiteral(typeSyntax: TypeSyntax, location: TextLocation): TypeSymbol {
        val boundType = when (val type = scope.tryLookup(typeSyntax.typeIdentifier.literal, typeSyntax.syntaxTree)) {
            is TypeSymbol -> {
                type
            }

            is StructSymbol -> {
                TypeSymbol.Struct(type.simpleName)
            }

            else -> {
                diagnosticsBag.reportUndefinedType(location, typeSyntax.typeIdentifier.literal)
                TypeSymbol.Error
            }
        }
        if (typeSyntax.isPointer) {
            return boundType.ref(
                typeSyntax.pointer!!.mutability != null
            )
        }
        return boundType
    }


}