package evaluator

import binding.Binder
import org.junit.jupiter.api.Test
import syntax.parser.Parser
import kotlin.test.assertEquals

class EvaluatorTest {
    private lateinit var evaluator: Evaluator

    private fun initEvaluator(input: String) {
        val parser = Parser(input)
        val syntaxTree = parser.parse()
        val binder = Binder()
        val boundExpression = binder.bindSyntaxTree(syntaxTree)
        evaluator = Evaluator(boundExpression)
    }

    @Test
    fun shouldEvaluateBasicPlusOperation() {
        initEvaluator("1 + 2")
        val result = evaluator.evaluate()
        assertEquals(3, result)
    }

    @Test
    fun shouldEvaluateOperationWithPlusAndMinus() {
        initEvaluator("1 + 2 - 3")
        val result = evaluator.evaluate()
        assertEquals(0, result)
    }

    @Test
    fun shouldEvaluateOperationWithPlusAndMinusAndAsterisk() {
        initEvaluator("1 + 2 - 3 * 4")
        val result = evaluator.evaluate()
        assertEquals(-9, result)
    }

    @Test
    fun shouldEvaluateOperationWithParenthesis() {
        initEvaluator("1 + (2 - 3) * 4")
        val result = evaluator.evaluate()
        assertEquals(-3, result)
    }

    @Test
    fun shouldEvaluateOperationWithUnaryOperator() {
        initEvaluator("-1 + 2")
        val result = evaluator.evaluate()
        assertEquals(1, result)
    }

    @Test
    fun shouldEvaluateOperationWithUnaryOperatorAndParenthesis() {
        initEvaluator("1 + -(2 - 3) * 4")
        val result = evaluator.evaluate()
        assertEquals(5, result)
    }

    @Test
    fun shouldHandleDoubleUnaryOperator() {
        initEvaluator("1 + --2")
        val result = evaluator.evaluate()
        assertEquals(3, result)
    }


    @Test
    fun shouldHandleMultipleUnaryOperator() {
        initEvaluator("1 + -2 * ---3")
        val result = evaluator.evaluate()
        assertEquals(7, result)
    }

    @Test
    fun shouldHandleMultipleUnaryOperatorWithParenthesis() {
        initEvaluator("1 + -2 * (---3)")
        val result = evaluator.evaluate()
        assertEquals(7, result)
    }

    @Test
    fun shouldEvaluateToTrue() {
        initEvaluator("true")
        val result = evaluator.evaluate()
        assertEquals(true, result)
    }

    @Test
    fun shouldEvaluateToFalse() {
        initEvaluator("false")
        val result = evaluator.evaluate()
        assertEquals(false, result)
    }


}