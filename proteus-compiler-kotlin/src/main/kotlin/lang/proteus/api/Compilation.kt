package lang.proteus.api

import lang.proteus.binding.Binder
import lang.proteus.binding.VariableContainer
import lang.proteus.evaluator.EvaluationResult
import lang.proteus.evaluator.Evaluator
import lang.proteus.syntax.parser.SyntaxTree

class Compilation(val syntaxTree: SyntaxTree) {
    fun evaluate(variables: Map<String, Any>): EvaluationResult<*> {

        val syntaxDiagnostics = syntaxTree.diagnostics
        val variableContainer = VariableContainer.fromUntypedMap(variables)
        val binder = Binder(variableContainer)
        val boundExpression = binder.bindSyntaxTree(syntaxTree)
        val diagnostics = binder.diagnostics.concat(syntaxDiagnostics)
        if (diagnostics.hasErrors()) {
            return EvaluationResult(diagnostics, null, variableContainer)
        }
        val evaluator = Evaluator(boundExpression, variableContainer)
        val value = evaluator.evaluate()
        return EvaluationResult(diagnostics, value, variableContainer)
    }
}