package lang.proteus.evaluator

import lang.proteus.binding.BoundUnaryOperator
import lang.proteus.symbols.TypeSymbol

internal object  UnaryExpressionEvaluator {
    fun evaluate(operator:  BoundUnaryOperator, operand: Any): Any {
        return when (operator) {
            BoundUnaryOperator.BoundUnaryIdentityOperator -> operand as Int
            BoundUnaryOperator.BoundUnaryNegationOperator -> -(operand as Int)
            BoundUnaryOperator.BoundUnaryNotOperator -> !(operand as Boolean)
            BoundUnaryOperator.BoundUnaryTypeOfOperator -> TypeSymbol.fromValueOrAny(operand)
            BoundUnaryOperator.BoundDereferenceOperator -> TODO()
            BoundUnaryOperator.BoundReferenceOperator -> TODO()
        }
    }
}