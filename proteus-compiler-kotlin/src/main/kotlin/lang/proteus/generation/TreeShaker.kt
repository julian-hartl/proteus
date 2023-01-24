package lang.proteus.generation

import lang.proteus.binding.*
import lang.proteus.symbols.FunctionSymbol

internal class TreeShaker private constructor(
    private val statement: BoundBlockStatement,
    private val scope: BoundGlobalScope,
) : BoundTreeRewriter() {

    companion object {
        fun shake( scope: BoundGlobalScope): BoundGlobalScope {
            val treeShaker = TreeShaker(scope.statement, scope)
            return treeShaker.shake()
        }
    }

    private val calledFunctions = mutableSetOf<FunctionSymbol>()

    private fun shake(): BoundGlobalScope {
        rewriteBlockStatement(statement)
        val declaredFunctions = scope.functions.toMutableList()
        val unusedFunctions = declaredFunctions.filter { it !in calledFunctions && it.name != "main" }
        unusedFunctions.forEach { declaredFunctions.remove(it) }
        return scope.copy(functions = declaredFunctions)
    }

    override fun rewriteCallExpression(expression: BoundCallExpression): BoundExpression {
        calledFunctions.add(expression.functionSymbol)
        return super.rewriteCallExpression(expression)
    }

}