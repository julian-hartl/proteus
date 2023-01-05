package lang.proteus.api.performance

internal class ComputationTimeStopper {
    private var startTime: Long = 0
    fun start() {
        startTime = System.currentTimeMillis()
    }

    fun stop(): ComputationTime {
        return ComputationTime(System.currentTimeMillis() - startTime)
    }
}