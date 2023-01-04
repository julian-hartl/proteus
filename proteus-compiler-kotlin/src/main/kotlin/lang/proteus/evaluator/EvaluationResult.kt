package lang.proteus.evaluator

import lang.proteus.diagnostics.Diagnostics

data class EvaluationResult<T>(val diagnostics: Diagnostics, val value: T)