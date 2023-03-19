package lang.proteus.generation.optimization

import lang.proteus.binding.BoundBlockStatement
import lang.proteus.binding.BoundExpression

internal interface Optimizer {
    fun optimize(statement: BoundBlockStatement): BoundBlockStatement
    fun optimizeExpression(expression: BoundExpression): BoundExpression
}

internal class CodeOptimizer {
    companion object {
        fun optimize(
            statement: BoundBlockStatement,
        ): BoundBlockStatement {
            val optimizer = CodeOptimizer()
            return optimizer.optimize(statement)
        }

        fun optimize(statement: BoundExpression): BoundExpression {
            val optimizer = CodeOptimizer()
            return optimizer.optimizeExpression(statement)
        }
    }

    private val optimizers = lazy<List<Optimizer>> {
        listOf(
            ConstantFolding(),
            DeadCodeElimination(),
        )
    }


    private fun optimize(statement: BoundBlockStatement): BoundBlockStatement {
        var lastStatement = statement
        for (optimizer in optimizers.value) {
            lastStatement = optimizer.optimize(lastStatement)
        }
        return lastStatement
    }

    private fun optimizeExpression(expression: BoundExpression): BoundExpression {
        var lastExpression = expression
        while (true) {
            var changed = false
            for (optimizer in optimizers.value) {
                val optimized = optimizer.optimizeExpression(lastExpression)
                if (optimized != lastExpression) {
                    changed = true
                    lastExpression = optimized
                }
            }
            if (!changed) {
                break
            }
        }
        return lastExpression
    }


}