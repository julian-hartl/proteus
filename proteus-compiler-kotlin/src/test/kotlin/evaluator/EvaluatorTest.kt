package evaluator

import lang.proteus.binding.Binder
import lang.proteus.binding.VariableContainer
import lang.proteus.evaluator.Evaluator
import lang.proteus.syntax.parser.Parser
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class EvaluatorTest {
    private lateinit var evaluator: Evaluator

    companion object {
        private const val TEST_VARIABLE_NAME = "x"
        private const val TEST_VARIABLE_VALUE = 1
    }

    private lateinit var variableContainer: VariableContainer

    private fun initEvaluator(input: String) {
        val variables: Map<String, Any> = mapOf(
            TEST_VARIABLE_NAME to TEST_VARIABLE_VALUE
        )
        val parser = Parser(input)
        val syntaxTree = parser.parse()
        variableContainer = VariableContainer.fromUntypedMap(variables)
        val binder = Binder(
            variableContainer
        )
        val boundExpression = binder.bindSyntaxTree(syntaxTree)
        evaluator = Evaluator(boundExpression, variableContainer)
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


    @Test
    fun notFalseShouldBeTrue() {
        initEvaluator("not false")
        val result = evaluator.evaluate()
        assertEquals(true, result)
    }

    @Test
    fun notTrueShouldBeFalse() {
        initEvaluator("not true")
        val result = evaluator.evaluate()
        assertEquals(false, result)
    }


    @Test
    fun shouldEvaluateLogicalAnd() {
        initEvaluator("true and false")
        val result = evaluator.evaluate()
        assertEquals(false, result)
    }

    @Test
    fun shouldEvaluateLogicalOr() {
        initEvaluator("false or true")
        val result = evaluator.evaluate()
        assertEquals(true, result)
    }

    @Test
    fun shouldEvaluateLogicalOrWithParenthesis() {
        initEvaluator("true or (false and true)")
        val result = evaluator.evaluate()
        assertEquals(true, result)
    }

    @Test
    fun shouldEvaluateComplexExpression() {
        initEvaluator("not (true or false) and (true or false)")
        val result = evaluator.evaluate()
        assertEquals(false, result)
    }

    @Test
    fun shouldEvaluateComplexExpressionWithUnaryOperator() {
        initEvaluator("not (true or false) and (true or false) and not (true or false) and (true or false)")
        val result = evaluator.evaluate()
        assertEquals(false, result)
    }

    @Test
    fun shouldEvaluateXor() {
        initEvaluator("true xor false")
        val result = evaluator.evaluate()
        assertEquals(true, result)
    }

    @Test
    fun shouldEvaluateXorWithParenthesis() {
        initEvaluator("true xor (false and true)")
        val result = evaluator.evaluate()
        assertEquals(true, result)
    }

    @Test
    fun shouldEvaluateComplexExpressionWithXor() {
        initEvaluator("not (true or false) and (true or false) xor not (true or false) and (true or false)")
        val result = evaluator.evaluate()
        assertEquals(false, result)
    }

    @Test
    fun shouldEvaluateEquals() {
        initEvaluator("1 == 1")
        val result = evaluator.evaluate()
        assertEquals(true, result)
    }

    @Test
    fun equalsShouldBindWeakerThanArithmeticOperators() {
        initEvaluator("1 + 2 == 3")
        val result = evaluator.evaluate()
        assertEquals(true, result)
    }

    @Test
    fun equalsShouldBindStrongerThanAnd() {
        initEvaluator("1 + 2 == 3 and true")
        val result = evaluator.evaluate()
        assertEquals(true, result)
    }

    @Test
    fun equalsShouldNotBindStrongerThanNot() {
        initEvaluator("false == not true")
        val result = evaluator.evaluate()
        assertEquals(true, result)
    }

    @Test
    fun equalsShouldBeApplicableToBoolean() {
        initEvaluator("true == true")
        val result = evaluator.evaluate()
        assertEquals(true, result)
    }

    @Test
    fun equalsShouldBeApplicableToInt() {
        initEvaluator("1 == 1")
        val result = evaluator.evaluate()
        assertEquals(true, result)
    }

    @Test
    fun notEqualsShouldBeApplicableToBoolean() {
        initEvaluator("true != true")
        val result = evaluator.evaluate()
        assertEquals(false, result)
    }

    @Test
    fun notEqualsShouldBeApplicableToInt() {
        initEvaluator("1 != 1")
        val result = evaluator.evaluate()
        assertEquals(false, result)
    }

    @Test
    fun notEqualsShouldBeApplicableToBoolean2() {
        initEvaluator("true != false")
        val result = evaluator.evaluate()
        assertEquals(true, result)
    }

    @Test
    fun notEqualsShouldBeApplicableToInt2() {
        initEvaluator("1 != 2")
        val result = evaluator.evaluate()
        assertEquals(true, result)
    }

    @Test
    fun notEqualsShouldBindStrongerThanAnd() {
        initEvaluator("1 + 2 != 3 and true")
        val result = evaluator.evaluate()
        assertEquals(false, result)
    }

    @Test
    fun notEqualsShouldNotBindStrongerThanNot() {
        initEvaluator("false != not true")
        val result = evaluator.evaluate()
        assertEquals(false, result)
    }

    @Test
    fun notEqualsShouldBindWeakerThanArithmeticOperators() {
        initEvaluator("1 + 2 != 3")
        val result = evaluator.evaluate()
        assertEquals(false, result)
    }

    @Test
    fun shouldEvaluateLessThan() {
        initEvaluator("1 < 2")
        val result = evaluator.evaluate()
        assertEquals(true, result)
    }

    @Test
    fun shouldEvaluateLessThanWithParenthesis() {
        initEvaluator("6 < (2 + 3)")
        val result = evaluator.evaluate()
        assertEquals(false, result)
    }

    @Test
    fun shouldEvaluateGreaterThan() {
        initEvaluator("2 > 1")
        val result = evaluator.evaluate()
        assertEquals(true, result)
    }

    @Test
    fun shouldEvaluateGreaterThanOrEquals() {
        initEvaluator("2 >= 2")
        val result = evaluator.evaluate()
        assertEquals(true, result)
    }

    @Test
    fun shouldEvaluateLessThanOrEquals() {
        initEvaluator("2 <= 2")
        val result = evaluator.evaluate()
        assertEquals(true, result)
    }

    @Test
    fun shouldEvaluatePower() {
        initEvaluator("2 ^^ 2")
        val result = evaluator.evaluate()
        assertEquals(4, result)
    }

    @Test
    fun shouldEvalXor() {
        initEvaluator("true xor false")
        val result = evaluator.evaluate()
        assertEquals(true, result)
    }

    @Test
    fun shouldEvalXor2() {
        initEvaluator("false xor true")
        val result = evaluator.evaluate()
        assertEquals(true, result)
    }

    @Test
    fun shouldEvalXor3() {
        initEvaluator("false xor false")
        val result = evaluator.evaluate()
        assertEquals(false, result)
    }

    @Test
    fun shouldEvalXor4() {
        initEvaluator("true xor true")
        val result = evaluator.evaluate()
        assertEquals(false, result)
    }

    @Test
    fun shouldEvalBitwiseXor() {
        initEvaluator("1 ^ 2")
        val result = evaluator.evaluate()
        assertEquals(3, result)
    }

    @Test
    fun shouldEvalRightShift() {
        initEvaluator("1 >> 1")
        val result = evaluator.evaluate()
        assertEquals(0, result)
    }

    @Test
    fun shouldEvalLeftShift() {
        initEvaluator("1 << 1")
        val result = evaluator.evaluate()
        assertEquals(2, result)
    }

    @Test
    fun isShouldEvalToTrueIfUsedWithSameType() {
        initEvaluator("1 is Int")
        val result = evaluator.evaluate()
        assertEquals(true, result)
    }

    @Test
    fun isShouldEvalToFalseIfUsedWithDifferentType() {
        initEvaluator("1 is Boolean")
        val result = evaluator.evaluate()
        assertEquals(false, result)
    }

    @Test
    fun isShouldBindStrongerThanAnd() {
        initEvaluator("1 is Int and true")
        val result = evaluator.evaluate()
        assertEquals(true, result)
    }

    @Test
    fun shouldAssignValueToVariable() {
        initEvaluator("$TEST_VARIABLE_NAME = 3")
        assertEquals(3, evaluator.evaluate())
        assertEquals(3, variableContainer.getVariableValue(TEST_VARIABLE_NAME))
    }

    @Test
    fun shouldUseVariableValueToComputeResult() {
        initEvaluator("$TEST_VARIABLE_NAME + 1")
        assertEquals(TEST_VARIABLE_VALUE + 1, evaluator.evaluate())
    }


}