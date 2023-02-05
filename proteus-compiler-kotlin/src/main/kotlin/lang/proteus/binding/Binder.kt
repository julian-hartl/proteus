package lang.proteus.binding

import lang.proteus.diagnostics.Diagnosable
import lang.proteus.diagnostics.DiagnosticsBag
import lang.proteus.diagnostics.TextLocation
import lang.proteus.generation.Lowerer
import lang.proteus.generation.Optimizer
import lang.proteus.grammar.ProteusParser
import lang.proteus.grammar.ProteusParser.BlockStatementContext
import lang.proteus.grammar.ProteusParser.ExpressionContext
import lang.proteus.grammar.ProteusParserBaseVisitor
import lang.proteus.symbols.*
import org.antlr.v4.runtime.ParserRuleContext
import java.util.*


internal class Binder(
    private var scope: BoundScope,
    private val function: FunctionSymbol?,
    private val module: Module,
    private val constantPool: ConstantPool,
) : Diagnosable {


    init {
        if (function != null) {
            for (parameter in function.parameters) {
                scope.tryDeclareVariable(parameter, module)
            }
        }
    }

    private val controlStructureStack: Stack<Unit> = Stack()


    companion object {


        fun bindGlobalScope(previous: BoundGlobalScope?, entryPointModule: Module): BoundGlobalScope {
            val parentScope = createParentScopes(previous)
            val binder =
                Binder(
                    parentScope ?: BoundScope(null),
                    null,
                    module = entryPointModule,
                    constantPool = ConstantPool(),
                )
            val diagnostics = DiagnosticsBag()
            val importGraph = ModuleGraph.create(entryPointModule)
            val cycles = importGraph.findCycles()
            for (cycle in cycles) {
                diagnostics.reportCircularDependency(cycle)
            }
            val modules = importGraph.getModules()
//            for (tree in modules) {
//                diagnostics.addAll(tree.diagnostics)
//            }
            for (module in modules) {
                val symbols = mutableSetOf<Symbol>()
                symbols.addAll(importGraph.gatherExportedSymbols(module))
                symbols.addAll(importGraph.gatherImportedSymbols(module))
                for (symbol in symbols) {
                    if (symbol is FunctionSymbol) {
                        binder.scope.tryDeclareFunction(symbol, module)
                    } else if (symbol is VariableSymbol) {
                        binder.scope.tryDeclareVariable(symbol, module)
                    }
                }
            }
            diagnostics.concat(importGraph.diagnosticsBag)

            diagnostics.addAll(binder.diagnostics)
            val variables = binder.scope.getDeclaredVariables()
            val functions = binder.scope.getDeclaredFunctions()
            if (previous != null) {
                diagnostics.addAll(previous.diagnostics)
            }
            val globalScope = BoundGlobalScope(previous, diagnostics.diagnostics, functions, variables)
            globalScope.importConstantPool(previous?.constantPool ?: ConstantPool())
            return globalScope
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
                    for (variable in variables) scope.tryDeclareVariable(variable, syntaxTree)
                }
                for ((syntaxTree, functions) in previous.mappedFunctions) {
                    for (function in functions) scope.tryDeclareFunction(function, syntaxTree)
                }


                parent = scope
            }
            return parent;
        }

        fun bindProgram(
            globalScope: BoundGlobalScope,
            module: Module,
            optimize: Boolean = true,
            functionBodySyntax: Map<FunctionSymbol, BlockStatementContext>,
            variableInitializerSyntax: Map<GlobalVariableSymbol, ProteusParser.ExpressionContext>,
        ): BoundProgram {

            val parentScope = createParentScopes(globalScope)

            val functionBodies = mutableMapOf<FunctionSymbol, BoundBlockStatement>()


            val diagnostics = DiagnosticsBag()
            diagnostics.addAll(globalScope.diagnostics)


            for ((module, functions) in globalScope.mappedFunctions) {
                for (function in functions) {
                    val binder = Binder(
                        parentScope ?: BoundScope(null),
                        function,
                        module,
                        globalScope.constantPool,
                    )
                    val functionBody: BlockStatementContext = functionBodySyntax[function]!!
                    val body = binder.walk(functionBody) as BoundBlockStatement
                    var optimizedBody = BoundBlockStatement(listOf())
                    if (!binder.hasErrors()) {
                        val loweredBody = Lowerer.lower(body)
                        optimizedBody = if (optimize) Optimizer.optimize(loweredBody) else loweredBody
                        val graph = ControlFlowGraph.createAndOutput(optimizedBody)
                        if (!graph.allPathsReturn()) {
                            diagnostics.reportAllCodePathsMustReturn(TextLocation(module, functionBody))
                        } else {
                            if (function.specifiedReturnType != null && function.specifiedReturnType != TypeSymbol.Unit){
                                for (incoming in graph.end.incoming) {
                                    if (incoming.statements.lastOrNull() !is BoundReturnStatement) {
                                        diagnostics.reportAllCodePathsMustReturn(TextLocation(module, functionBody))
                                    }
                                }
                            }
                        }
                        for (block in graph.blocks) {
                            if (block.isEnd != true && block.isStart != true && block.incoming.size == 0) {
                                diagnostics.reportUnreachableCode(TextLocation(module, functionBody));
                            }
                        }
                    }
                    functionBodies[function] = optimizedBody
                    diagnostics.addAll(binder.diagnostics)
                }
            }

            val variableInitializers = mutableMapOf<GlobalVariableSymbol, BoundExpression>()
            for ((module, variables) in globalScope.mappedVariables) {
                for (variable in variables) {
                    if (variable is GlobalVariableSymbol) {
                        val binder = Binder(parentScope ?: BoundScope(null), null, module, globalScope.constantPool)
                        val initializer = variableInitializerSyntax[variable]!!
                        val boundInitializer = binder.walk(initializer) as BoundExpression
                        val optimized = Optimizer.optimize(boundInitializer)
                        variableInitializers[variable] = optimized
                        diagnostics.addAll(binder.diagnostics)
                    }
                }
            }

            val mainFunction =
                validateMainFunction(globalScope.mappedFunctions[module] ?: emptySet(), diagnostics)


            return BoundProgram(
                globalScope, diagnostics.diagnostics, functionBodies, variableInitializers, mainFunction
            )
        }

        private fun validateMainFunction(functions: Set<FunctionSymbol>, diagnostics: DiagnosticsBag): FunctionSymbol {
            val mainFunction = functions.firstOrNull { it.simpleName == "main" }
            if (mainFunction == null) {
                throw IllegalStateException("No entry point found. Specify a main function.")
            } else if (mainFunction.parameters.isNotEmpty()) {
                diagnostics.reportMainMustHaveNoParameters(mainFunction)
            } else if (mainFunction.specifiedReturnType != TypeSymbol.Unit) {
                diagnostics.invalidMainFunctionReturnType(mainFunction, TypeSymbol.Unit)
            }
            return mainFunction
        }
    }


    private val diagnosticsBag = DiagnosticsBag()

    override val diagnostics = diagnosticsBag.diagnostics

    fun walk(ctx: ParserRuleContext): BoundNode {
        return ctx.accept(BoundTreeWalker())
    }

    private inner class BoundTreeWalker : ProteusParserBaseVisitor<BoundNode>() {

        override fun visitReturnStatement(ctx: ProteusParser.ReturnStatementContext): BoundReturnStatement {
            val expression = ctx.expression()
            val statement = if (expression == null) null else visitExpression(expression)
            val actualReturnType = statement?.type ?: TypeSymbol.Unit
            if (!isInsideFunction()) {
                diagnosticsBag.reportReturnNotAllowed(TextLocation(module, ctx.RETURN().symbol))
            } else {
                val functionReturnType = function!!.returnType
                val conversion = Conversion.classify(actualReturnType, functionReturnType)
                if (conversion.isNone) {
                    diagnosticsBag.reportInvalidReturnType(
                        if (expression != null) TextLocation(module, expression) else TextLocation(
                            module, ctx.RETURN().symbol
                        ), functionReturnType, actualReturnType
                    )
                }
            }
            return BoundReturnStatement(statement, actualReturnType)
        }


        override fun visitExpression(ctx: ProteusParser.ExpressionContext): BoundExpression {
            val isUnary = ctx.prefix != null
            val isBinary = ctx.bop != null
            if (isUnary) {
                val operator = ctx.prefix.text
                val operand = visitExpression(ctx.expression(0))
                val boundUnaryOperator = BoundUnaryOperator.bind(operator, operand.type)
                if (boundUnaryOperator == null) {
                    diagnosticsBag.reportUnaryOperatorMismatch(operator, TextLocation(module, ctx.prefix), operand.type)
                    return BoundErrorExpression
                }
                return BoundUnaryExpression(operand, boundUnaryOperator)
            }
            if (isBinary) {
                val leftSyntax = ctx.expression(0)
                val left = visitExpression(leftSyntax)
                val rightSyntax = ctx.expression(1)
                val right = visitExpression(rightSyntax)
                val operator = ctx.bop.text
                val boundBinaryOperator = BoundBinaryOperator.bind(operator, left.type, right.type)
                if (boundBinaryOperator == null) {
                    diagnosticsBag.reportBinaryOperatorMismatch(
                        operator, TextLocation(module, ctx.bop), left.type, right.type
                    )
                    return BoundErrorExpression
                }
                val isAssignment = when (boundBinaryOperator.kind) {
                    BoundBinaryOperatorKind.Assignment -> true
                    BoundBinaryOperatorKind.AdditionAssignment -> true
                    BoundBinaryOperatorKind.SubtractionAssignment -> true
                    else -> false
                }
                if (isAssignment) {
                    val variableName = (left as BoundVariableExpression).variable.simpleName
                    val declaredVariable = scope.tryLookupVariable(variableName, module)
                    if (declaredVariable == null) {
                        diagnosticsBag.reportUnresolvedReference(TextLocation(module, leftSyntax), variableName)
                        return BoundErrorExpression
                    }
                    if (declaredVariable.isReadOnly) {
                        diagnosticsBag.reportFinalVariableCannotBeReassigned(
                            TextLocation(module, leftSyntax), declaredVariable
                        )
                        return BoundErrorExpression
                    }
                    val convertedExpression = convertToType(rightSyntax, right, declaredVariable.type)
                    return BoundAssignmentExpression(
                        declaredVariable, convertedExpression, boundBinaryOperator, returnAssignment = true
                    )
                }
                return BoundBinaryExpression(left, right, boundBinaryOperator)
            }
            return super.visitChildren(ctx) as BoundExpression
        }

        override fun visitMethodCall(ctx: ProteusParser.MethodCallContext): BoundNode {
            val functionName = ctx.identifier().text
            val declaredFunction = scope.tryLookupFunction(functionName, module)
            if (declaredFunction == null) {
                diagnosticsBag.reportUndefinedFunction(TextLocation(module, ctx.identifier()), functionName)
                return BoundErrorExpression
            }
            val argumentsCount = ctx.expressionList().expression().size
            val parameterCount = declaredFunction.parameters.size
            if (argumentsCount < parameterCount) {
                val location = TextLocation(module, ctx.expressionList().expression(0).start)
                diagnosticsBag.reportTooFewArguments(
                    location, functionName, parameterCount, argumentsCount
                )
                return BoundErrorExpression
            }
            if (argumentsCount > parameterCount) {
                val count = argumentsCount - parameterCount
                val location = TextLocation(module, ctx.expressionList().expression(count).start)
                diagnosticsBag.reportTooManyArguments(
                    location, functionName, parameterCount, argumentsCount
                )
                return BoundErrorExpression
            }
            val boundParameters: MutableList<BoundExpression> = mutableListOf()
            for ((index, parameter) in declaredFunction.parameters.withIndex()) {

                val argument = ctx.expressionList().expression()[index]
                val boundArgument = visitExpression(argument)
                if (boundArgument.type == TypeSymbol.Error) return BoundErrorExpression
                if (!boundArgument.type.isAssignableTo(parameter.type)) {
                    diagnosticsBag.reportCannotConvert(
                        TextLocation(module, argument), parameter.type, boundArgument.type
                    )
                    return BoundErrorExpression
                }
                boundParameters.add(boundArgument)
            }


            return BoundCallExpression(declaredFunction, boundParameters)
        }

        override fun visitLiteral(ctx: ProteusParser.LiteralContext): BoundLiteralExpression<*> {
            val isBoolean = ctx.BOOL_LITERAL() != null
            if (isBoolean) {
                val value = ctx.BOOL_LITERAL().text.toBoolean()
                return BoundLiteralExpression(value)
            }
            val isString = ctx.STRING_LITERAL() != null
            if (isString) {
                val value = ctx.STRING_LITERAL().text
                return BoundLiteralExpression(value)
            }
            val isInt = ctx.INT_LITERAL() != null
            if (isInt) {
                val value = ctx.INT_LITERAL().text.toInt()
                return BoundLiteralExpression(value)
            }
            throw IllegalStateException("Unexpected literal type")
        }

        override fun visitContinueStatement(ctx: ProteusParser.ContinueStatementContext): BoundContinueStatement {
            val isInsideLoop = isInsideLoop()
            if (!isInsideLoop) {
                diagnosticsBag.reportContinueOutsideLoop(TextLocation(module, ctx))
            }
            return BoundContinueStatement()
        }

        override fun visitBreakStatement(ctx: ProteusParser.BreakStatementContext): BoundBreakStatement {
            if (!isInsideLoop()) {
                diagnosticsBag.reportBreakOutsideLoop(TextLocation(module, ctx))
            }
            return BoundBreakStatement()
        }

        override fun visitForStatement(ctx: ProteusParser.ForStatementContext): BoundNode {
            val forControl = ctx.forControl()
            val lowerBoundSyntax = forControl.INT_LITERAL(0)
            val boundLower = BoundLiteralExpression(lowerBoundSyntax.text.toInt())
            val upperBoundSyntax = forControl.INT_LITERAL(1)
            val boundUpper = BoundLiteralExpression(upperBoundSyntax.text.toInt())

            if (boundLower.type != TypeSymbol.Int) {
                diagnosticsBag.reportCannotConvert(
                    TextLocation(module, lowerBoundSyntax.symbol), TypeSymbol.Int, boundLower.type
                )
            }

            if (boundUpper.type != TypeSymbol.Int) {
                diagnosticsBag.reportCannotConvert(
                    TextLocation(module, upperBoundSyntax.symbol), TypeSymbol.Int, boundUpper.type
                )
            }

            scope = BoundScope(scope)

            val name = forControl.identifier().IDENTIFIER().text
            val variable = LocalVariableSymbol(
                name, TypeSymbol.Int, null, isFinal = true, isConst = false, module.moduleReference,
            )
            val declaredVariable = scope.tryLookupVariable(name, module)
            if (declaredVariable != null) {
                diagnosticsBag.reportVariableAlreadyDeclared(TextLocation(module, forControl.identifier()), name)
            }

            scope.tryDeclareVariable(variable, module)
            controlStructureStack.push(Unit)
            val body = visitStatement(ctx.statement())
            controlStructureStack.pop()
            scope = scope.parent!!
            return BoundForStatement(variable, boundLower, boundUpper, body)
        }

        override fun visitWhileStatement(ctx: ProteusParser.WhileStatementContext): BoundWhileStatement {
            val condition: BoundExpression = visitExpressionWithType(ctx.expression(), TypeSymbol.Boolean)
            controlStructureStack.push(Unit)
            scope = BoundScope(scope)
            val body = visitStatement(ctx.statement())
            scope = scope.parent!!
            controlStructureStack.pop()
            return BoundWhileStatement(condition, body)
        }

        override fun visitIfStatement(ctx: ProteusParser.IfStatementContext): BoundNode {
            val condition: BoundExpression = visitExpressionWithType(ctx.expression(), TypeSymbol.Boolean)
            scope = BoundScope(scope)
            val thenStatement = visitStatement(ctx.statement(0))
            scope = scope.parent!!
            val elseStatement = ctx.statement().getOrNull(1)?.let { visitStatement(it) }
            return BoundIfStatement(condition, thenStatement, elseStatement)
        }

        override fun visitStatement(ctx: ProteusParser.StatementContext?): BoundStatement {
            return super.visitStatement(ctx) as BoundStatement
        }

        private fun visitExpressionWithType(
            expression: ProteusParser.ExpressionContext,
            expectedType: TypeSymbol,
        ): BoundExpression {
            val boundExpression = visitExpression(expression)
            if (boundExpression.type == TypeSymbol.Error) return boundExpression
            if (!boundExpression.type.isAssignableTo(expectedType)) {
                diagnosticsBag.reportCannotConvert(
                    TextLocation(module, expression), expectedType, boundExpression.type
                )
                return BoundErrorExpression
            }
            return boundExpression
        }

        override fun visitVariableDeclaration(ctx: ProteusParser.VariableDeclarationContext): BoundVariableDeclaration {
            val symbol = VariableDeclarationSymbolParser.parse(ctx, module, isGlobal = function == null)
            val initializer = visitExpression(ctx.expression())
            val convertedInitializer = convertToType(ctx.expression(), initializer, symbol.type)
            if (symbol.isConst) {
                if (isExpressionConst(convertedInitializer)) {
                    constantPool.add(symbol, convertedInitializer)
                } else {
                    diagnosticsBag.reportExpectedConstantExpression(TextLocation(module, ctx.expression()))
                }
            }
            val isVariableAlreadyDeclared = scope.tryDeclareVariable(symbol, module) == null
            if (isVariableAlreadyDeclared) {
                diagnosticsBag.reportVariableAlreadyDeclared(TextLocation(module, ctx.identifier()), symbol.simpleName)
            }
            return BoundVariableDeclaration(symbol, convertedInitializer)
        }

        override fun visitBlockStatement(ctx: ProteusParser.BlockStatementContext): BoundNode {
            scope = BoundScope(scope)
            val statements = ctx.statement().map {
                val statement = visitStatement(it)
                if (statement is BoundExpressionStatement) {
                    val expression = statement.expression
                    if (expression !is BoundCallExpression && expression !is BoundAssignmentExpression && expression !is BoundErrorExpression) {
                        diagnosticsBag.reportUnusedExpression(TextLocation(module, it))
                    }
                }
                statement
            }
            scope = scope.parent!!
            return BoundBlockStatement(
                statements
            )
        }

        override fun visitIdentifier(ctx: ProteusParser.IdentifierContext): BoundExpression {
            val name = ctx.IDENTIFIER().text
            val variable = scope.tryLookupVariable(name, module)
            if (variable == null) {
                diagnosticsBag.reportUnresolvedReference(TextLocation(module, ctx), name)
                return BoundErrorExpression
            }
            return BoundVariableExpression(variable)
        }

        private fun convertToType(
            expressionSyntax: ExpressionContext,
            boundExpression: BoundExpression,
            type: TypeSymbol,
        ): BoundExpression {
            val conversion = Conversion.classify(boundExpression.type, type)
            if (conversion.isIdentity) return boundExpression
            if (conversion.isImplicit) return BoundConversionExpression(type, boundExpression, conversion)
            if (conversion.isExplicit) return BoundConversionExpression(type, boundExpression, conversion)
            diagnosticsBag.reportCannotConvert(TextLocation(module, expressionSyntax), type, boundExpression.type)
            return BoundErrorExpression
        }

    }


    private fun isInsideFunction(): Boolean {
        return function != null
    }


    private fun isInsideLoop(): Boolean {
        return controlStructureStack.isNotEmpty()
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


}