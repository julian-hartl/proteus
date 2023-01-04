package lang.proteus.api

import lang.proteus.binding.Binder
import lang.proteus.evaluator.EvaluationResult
import lang.proteus.evaluator.Evaluator
import lang.proteus.syntax.parser.SyntaxTree

class Compilation(val syntaxTree: SyntaxTree) {
    fun evaluate(variables: MutableMap<String, Any>): EvaluationResult<*> {

        val syntaxDiagnostics = syntaxTree.diagnostics
        val binder = Binder(variables)
        val boundExpression = binder.bindSyntaxTree(syntaxTree)
        val diagnostics = binder.diagnostics.concat(syntaxDiagnostics)
        if (diagnostics.hasErrors()) {
            return EvaluationResult(diagnostics, null)
        }
        val evaluator = Evaluator(boundExpression, variables)
        val value = evaluator.evaluate()
        return EvaluationResult(diagnostics, value)
    }
}