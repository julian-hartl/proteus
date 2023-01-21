package lang.proteus.api.performance

import java.lang.management.MemoryUsage

data class CompilationPerformance(
    val lexingTime: ComputationTime,
    val parsingTime: ComputationTime,
    val evaluationTime: ComputationTime,
    val memoryUsage: Int,
)