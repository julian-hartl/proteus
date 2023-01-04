package lang.proteus.api

import lang.proteus.diagnostics.MutableDiagnostics
import lang.proteus.evaluator.EvaluationResult
import lang.proteus.syntax.parser.SyntaxTree

class Compilation(val syntaxTree: SyntaxTree) {
    fun evaluate(): EvaluationResult {

        return EvaluationResult(MutableDiagnostics())
    }
}