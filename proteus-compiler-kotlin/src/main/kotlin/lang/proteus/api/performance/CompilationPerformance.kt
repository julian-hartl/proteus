package lang.proteus.api.performance

data class CompilationPerformance(
    val lexingTime: ComputationTime,
    val parsingTime: ComputationTime,
    val evaluationTime: ComputationTime,
    val codeGenerationTime: ComputationTime,
    val memoryUsage: Int,
)