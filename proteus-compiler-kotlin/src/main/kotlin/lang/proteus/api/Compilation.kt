package lang.proteus.api

import lang.proteus.binding.Binder
import lang.proteus.evaluator.EvaluationResult
import lang.proteus.evaluator.Evaluator
import lang.proteus.syntax.parser.SyntaxTree

class Compilation(val syntaxTree: SyntaxTree) {
    fun evaluate(): EvaluationResult<*> {

        val syntaxDiagnostics = syntaxTree.diagnostics
        val binder = Binder()
        val boundExpression = binder.bindSyntaxTree(syntaxTree)
        val evaluator = Evaluator(boundExpression)
        val value = evaluator.evaluate()
        val diagnostics = binder.diagnostics.concat(syntaxDiagnostics)
        return EvaluationResult(diagnostics, value)
    }
}