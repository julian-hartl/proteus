package lang.proteus.evaluator

import lang.proteus.api.performance.ComputationTime
import lang.proteus.diagnostics.Diagnostics

data class EvaluationResult<T>(
    val diagnostics: Diagnostics,
    val value: T?,
    val parseTime: ComputationTime,
    val evaluationTime: ComputationTime,
    val generatedCode: String?
)