package lang.proteus.binding

import lang.proteus.diagnostics.Diagnosable
import lang.proteus.diagnostics.DiagnosticsBag
import lang.proteus.diagnostics.TextSpan
import lang.proteus.symbols.ProteusExternalFunction
import lang.proteus.symbols.TypeSymbol
import lang.proteus.symbols.VariableSymbol
import lang.proteus.syntax.lexer.token.Keyword
import lang.proteus.syntax.parser.*
import lang.proteus.syntax.parser.statements.*
import java.util.*

internal class Binder(private var scope: BoundScope) : Diagnosable {


    companion object {
        fun bindGlobalScope(previous: BoundGlobalScope?, syntax: CompilationUnitSyntax): BoundGlobalScope {
            val parentScope = createParentScopes(previous)
            val binder = Binder(parentScope ?: BoundScope(null))
            val boundExpression = binder.bindStatement(syntax.statement)
            val variables = binder.scope.getDeclaredVariables()
            val diagnostics = binder.diagnostics
            if (previous != null) {
                diagnostics.concat(previous.diagnostics)
            }
            return BoundGlobalScope(previous, diagnostics, variables, boundExpression)
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

                parent = scope
            }
            return parent;
        }
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
        }
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
        val variable = VariableSymbol(name, TypeSymbol.Int, isFinal = true)
        val declaredVariable = scope.tryLookupVariable(name)
        if (declaredVariable != null) {
            diagnosticsBag.reportVariableAlreadyDeclared(syntax.identifier.span(), name)
        }

        scope.tryDeclareVariable(variable)
        val body = bindStatement(syntax.body)
        scope = scope.parent!!
        return BoundForStatement(variable, boundLower, syntax.rangeOperator.token, boundUpper, body)
    }

    private fun bindWhileStatement(syntax: WhileStatementSyntax): BoundStatement {
        val condition = bindExpressionWithType(syntax.condition, TypeSymbol.Boolean)
        val body = bindStatement(syntax.body)
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
            if (boundExpression.type == TypeSymbol.Error || expectedType == TypeSymbol.Error) {
                return BoundErrorExpression
            }
            diagnosticsBag.reportCannotConvert(textSpan, expectedType, boundExpression.type)
            return BoundErrorExpression
        }

        return BoundConversionExpression(expectedType, boundExpression, conversion)
    }

    private fun bindVariableDeclaration(syntax: VariableDeclarationSyntax): BoundStatement {
        val initializer = bindExpression(syntax.initializer)
        val isFinal = syntax.keyword is Keyword.Val
        val typeClause = bindTypeClause(syntax.typeClauseSyntax)
        val type = typeClause ?: initializer.type
        val convertedInitializer = bindConversion(initializer, type, syntax.initializer.span())
        val symbol = VariableSymbol(syntax.identifier.literal, type, isFinal)
        val isVariableAlreadyDeclared = scope.tryDeclareVariable(symbol) == null
        if (isVariableAlreadyDeclared) {
            diagnosticsBag.reportVariableAlreadyDeclared(syntax.identifier.span(), syntax.identifier.literal)
        }
        return BoundVariableDeclaration(symbol, convertedInitializer)
    }

    private fun bindTypeClause(syntax: TypeClauseSyntax?): TypeSymbol? {
        if (syntax == null) return null
        val type = TypeSymbol.fromName(syntax.type.literal)
        if (type == null) {
            diagnosticsBag.reportUndefinedType(syntax.type.span(), syntax.type.literal)
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
        val functionSymbol = ProteusExternalFunction.lookup(syntax.functionIdentifier.literal)?.symbol
        if (functionSymbol == null) {
            diagnosticsBag.reportUndefinedFunction(syntax.functionIdentifier.span(), syntax.functionIdentifier.literal)
            return BoundErrorExpression
        }
        if (syntax.arguments.count < functionSymbol.parameters.size) {
            diagnosticsBag.reportTooFewArguments(
                TextSpan(syntax.closeParenthesis.span().start - 1, 1),
                syntax.functionIdentifier.literal,
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
                syntax.functionIdentifier.literal,
                functionSymbol.parameters.size,
                syntax.arguments.count
            )
            return BoundErrorExpression
        }
        val boundParameters: MutableList<BoundExpression> = mutableListOf()
        for ((index, parameter) in functionSymbol.parameters.withIndex()) {

            val argument: ExpressionSyntax = syntax.arguments.get(index)
            val boundArgument = bindExpression(argument)
            if (!boundArgument.type.isAssignableTo(parameter.type)) {
                diagnosticsBag.reportCannotConvert(argument.span(), parameter.type, boundArgument.type)
                return BoundErrorExpression
            }
            boundParameters.add(boundArgument)
        }


        return BoundCallExpression(functionSymbol, boundParameters, isExternal = true)
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
        return BoundAssignmentExpression(declaredVariable, convertedExpression, assignmentOperator)
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