package lang.proteus.api

import lang.proteus.api.performance.CompilationPerformance
import lang.proteus.evaluator.EvaluationResult
import lang.proteus.syntax.parser.SyntaxTree

internal data class InterpretationResult(
    val syntaxTree: SyntaxTree,
    val evaluationResult: EvaluationResult<*>?,
    val performance: CompilationPerformance?
)