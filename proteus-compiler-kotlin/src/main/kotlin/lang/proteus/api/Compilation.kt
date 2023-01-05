package lang.proteus.api

import lang.proteus.api.performance.ComputationTime
import lang.proteus.api.performance.ComputationTimeStopper
import lang.proteus.binding.Binder
import lang.proteus.binding.VariableContainer
import lang.proteus.evaluator.EvaluationResult
import lang.proteus.evaluator.Evaluator
import lang.proteus.syntax.parser.SyntaxTree

class Compilation(val syntaxTree: SyntaxTree) {
    fun evaluate(variables: Map<String, Any>): EvaluationResult<*> {

        val computationTimeStopper = ComputationTimeStopper()
        computationTimeStopper.start()
        val syntaxDiagnostics = syntaxTree.diagnostics
        val variableContainer = VariableContainer.fromUntypedMap(variables)
        val binder = Binder(variableContainer)
        val boundExpression = binder.bindSyntaxTree(syntaxTree)
        binder.diagnostics.concat(syntaxDiagnostics)
        val diagnostics = binder.diagnostics
        val parseTime = computationTimeStopper.stop()
        if (diagnostics.hasErrors()) {

            return EvaluationResult(diagnostics, null, variableContainer, parseTime, ComputationTime(0))
        }
        computationTimeStopper.start()
        val evaluator = Evaluator(boundExpression, variableContainer)
        val value = evaluator.evaluate()
        val evaluationTime = computationTimeStopper.stop()
        return EvaluationResult(diagnostics, value, variableContainer, parseTime, evaluationTime)
    }
}