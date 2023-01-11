package lang.proteus.binding

import lang.proteus.diagnostics.Diagnosable
import lang.proteus.diagnostics.DiagnosticsBag
import lang.proteus.syntax.lexer.token.Keyword
import lang.proteus.syntax.lexer.token.Operator
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
                    scope.tryDeclare(variable)
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
        val boundIteratorExpression = bindExpressionWithType(syntax.iteratorExpression, ProteusType.Range)

        scope = BoundScope(scope)

        val name = syntax.identifier.literal
        val variable = VariableSymbol(name, ProteusType.Int, isFinal = true)
        val declaredVariable = scope.tryLookup(name)
        if (declaredVariable != null) {
            diagnosticsBag.reportVariableAlreadyDeclared(syntax.identifier.span(), name)
        }

        scope.tryDeclare(variable)
        val body = bindStatement(syntax.body)
        scope = scope.parent!!
        return BoundForStatement(variable, boundIteratorExpression, body)
    }

    private fun bindWhileStatement(syntax: WhileStatementSyntax): BoundStatement {
        val condition = bindExpressionWithType(syntax.condition, ProteusType.Boolean)
        val body = bindStatement(syntax.body)
        return BoundWhileStatement(condition, body)
    }

    private fun bindIfStatement(syntax: IfStatementSyntax): BoundStatement {
        val condition = bindExpressionWithType(syntax.condition, ProteusType.Boolean)
        val thenStatement = bindStatement(syntax.thenStatement)
        val elseStatement = syntax.elseClause?.let { bindStatement(it.elseStatementSyntax) }
        return BoundIfStatement(condition, thenStatement, elseStatement)
    }

    private fun bindExpressionWithType(syntax: ExpressionSyntax, expectedType: ProteusType): BoundExpression {
        val expression = bindExpression(syntax)
        if (expression.type != expectedType) {
            diagnosticsBag.reportCannotConvert(syntax.span(), expectedType, expression.type)
        }
        return expression
    }

    private fun bindVariableDeclaration(syntax: VariableDeclarationSyntax): BoundStatement {
        val boundExpression = bindExpression(syntax.initializer)
        val isFinal = syntax.keyword is Keyword.Val
        val symbol = VariableSymbol(syntax.identifier.literal, boundExpression.type, isFinal)
        val isVariableAlreadyDeclared = scope.tryDeclare(symbol) == null
        if (isVariableAlreadyDeclared) {
            diagnosticsBag.reportVariableAlreadyDeclared(syntax.identifier.span(), syntax.identifier.literal)
        }
        return BoundVariableDeclaration(symbol, boundExpression)
    }

    private fun bindExpressionStatement(syntax: ExpressionStatementSyntax): BoundStatement {
        val boundExpression = bindExpression(syntax.expression)
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

    fun bindExpression(syntax: ExpressionSyntax): BoundExpression {
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
        }
    }

    private fun bindAssignmentExpression(syntax: AssignmentExpressionSyntax): BoundExpression {
        val assignmentOperator = syntax.assignmentOperator.token
        val boundExpression = bindExpression(syntax.expression)
        val variableName = syntax.identifierToken.literal
        val variableType = boundExpression.type
        val declaredVariable = scope.tryLookup(variableName)
        if (declaredVariable == null) {
            diagnosticsBag.reportUnresolvedReference(syntax.identifierToken.span(), variableName)
            return boundExpression
        }
        val typesAreApplicable = when (assignmentOperator) {
            is Operator.PlusEquals -> {

                BoundBinaryOperator.BoundAdditionBinaryOperator.canBeApplied(declaredVariable.type, variableType)
            }

            is Operator.MinusEquals -> {

                BoundBinaryOperator.BoundSubtractionBinaryOperator.canBeApplied(declaredVariable.type, variableType)
            }

            else -> true
        }
        if (!typesAreApplicable) {
            diagnosticsBag.reportCannotConvert(syntax.identifierToken.span(), declaredVariable.type, variableType)
        }
        if (declaredVariable.isFinal) {
            diagnosticsBag.reportFinalVariableCannotBeReassigned(syntax.identifierToken.span(), variableName)
        } else {
            if (!variableType.isAssignableTo(declaredVariable.type)) {
                diagnosticsBag.reportCannotConvert(syntax.expression.span(), declaredVariable.type, variableType)
            }
        }
        return BoundAssignmentExpression(declaredVariable, boundExpression, assignmentOperator)
    }

    private fun bindNameExpressionSyntax(syntax: NameExpressionSyntax): BoundExpression {
        val name = syntax.identifierToken.literal
        if (name.isEmpty()) {
            // Token was inserted by parser. We already reported that error so we can just return an error expression.
            return BoundLiteralExpression(0)
        }
        val variable = scope.tryLookup(name)
        if (variable == null) {
            diagnosticsBag.reportUnresolvedReference(syntax.identifierToken.span(), name)
            return BoundLiteralExpression(0)
        }
        return BoundVariableExpression(variable)
    }


    private fun bindBinaryExpression(binaryExpression: BinaryExpressionSyntax): BoundExpression {

        val boundLeft = bindExpression(binaryExpression.left)
        val boundRight = bindExpression(binaryExpression.right)
        val binaryOperator =
            BoundBinaryOperator.bind(binaryExpression.operatorToken.token, boundLeft.type, boundRight.type)
        if (binaryOperator == null) {
            diagnosticsBag.reportBinaryOperatorMismatch(
                binaryExpression.operatorToken.literal,
                binaryExpression.operatorToken.span(),
                boundLeft.type,
                boundRight.type
            )
            return boundLeft
        }
        return BoundBinaryExpression(boundLeft, boundRight, binaryOperator)

    }


    private fun bindUnaryExpression(unaryExpression: UnaryExpressionSyntax): BoundExpression {
        val boundOperand = bindExpression(unaryExpression.operand)
        val boundOperator = BoundUnaryOperator.bind(unaryExpression.operatorSyntaxToken.token, boundOperand.type)
        if (boundOperator == null) {
            diagnosticsBag.reportUnaryOperatorMismatch(
                unaryExpression.operatorSyntaxToken.literal,
                unaryExpression.operatorSyntaxToken.span(),
                boundOperand.type
            )
            return boundOperand
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