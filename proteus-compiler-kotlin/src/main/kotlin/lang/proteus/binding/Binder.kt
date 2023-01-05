package lang.proteus.binding

import lang.proteus.diagnostics.Diagnosable
import lang.proteus.diagnostics.DiagnosticsBag
import lang.proteus.syntax.parser.*
import java.util.*

internal class Binder(private var scope: BoundScope) : Diagnosable {


    companion object {
        fun bindGlobalScope(previous: BoundGlobalScope?, syntax: CompilationUnitSyntax): BoundGlobalScope {
            val parentScope = createParentScopes(previous)
            val binder = Binder(parentScope ?: BoundScope(null))
            val boundExpression = binder.bind(syntax.expression)
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

    fun bind(syntax: ExpressionSyntax): BoundExpression {
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
                bind(syntax.expressionSyntax)
            }

            is NameExpressionSyntax -> bindNameExpressionSyntax(syntax)
            is AssignmentExpressionSyntax -> bindAssignmentExpression(syntax)
        }
    }

    private fun bindAssignmentExpression(syntax: AssignmentExpressionSyntax): BoundExpression {
        val boundExpression = bind(syntax.expression)
        val variableName = syntax.identifierToken.literal
        val variableType = boundExpression.type
        val variableSymbol = VariableSymbol(variableName, variableType)
        val declaredVariable = scope.tryLookup(variableName)
        if (declaredVariable != null && declaredVariable.isFinal) {
            diagnosticsBag.reportFinalVariableCannotBeReassigned(syntax.identifierToken.span(), variableName)
        }
        if (declaredVariable != null) {
            if (!variableType.isAssignableTo(declaredVariable.type)) {
                diagnosticsBag.reportCannotConvert(syntax.equalsToken.span(), declaredVariable.type, variableType)
            } else {
                scope.tryDeclare(variableSymbol)
            }
        } else {
            scope.tryDeclare(variableSymbol)
        }
        return BoundAssignmentExpression(variableSymbol, boundExpression)
    }

    private fun bindNameExpressionSyntax(syntax: NameExpressionSyntax): BoundExpression {
        val name = syntax.identifierToken.literal
        val variable = scope.tryLookup(name)
        if (variable == null) {
            diagnosticsBag.reportUndeclaredVariable(syntax.identifierToken.span(), name)
            return BoundLiteralExpression(0)
        }
        return BoundVariableExpression(variable)
    }


    private fun bindBinaryExpression(binaryExpression: BinaryExpressionSyntax): BoundExpression {

        val boundLeft = bind(binaryExpression.left)
        val boundRight = bind(binaryExpression.right)
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
        val boundOperand = bind(unaryExpression.operand)
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