package lang.proteus.binder

import lang.proteus.binding.Binder
import lang.proteus.binding.VariableContainer
import org.junit.jupiter.api.Test
import lang.proteus.syntax.parser.ExpressionSyntax
import lang.proteus.syntax.parser.Parser
import lang.proteus.text.SourceText
import kotlin.test.assertTrue

class BinderTest {

    private lateinit var binder: Binder

    companion object {
        private const val TEST_VARIABLE_NAME = "x"
        private const val TEST_VARIABLE_VALUE = 1
    }

    private fun useExpression(input: kotlin.String) {
        val expression = parseExpression(input)
        val variables: MutableMap<kotlin.String, Any> = mutableMapOf(
            TEST_VARIABLE_NAME to TEST_VARIABLE_VALUE
        )
        binder = Binder(VariableContainer.fromUntypedMap(variables))
        binder.bind(expression)
    }

    private fun parseExpression(input: kotlin.String): ExpressionSyntax {
        val parser = Parser(SourceText.from(input))
        val syntaxTree = parser.parse()
        return syntaxTree.root
    }

    @Test
    fun shouldHaveErrorWhenMultiplyingBooleans() {
        useExpression("true * false")
        assertTrue(binder.hasErrors())
    }

    @Test
    fun shouldHaveErrorWhenAddingBooleans() {
        useExpression("true + false")
        assertTrue(binder.hasErrors())
    }

    @Test
    fun shouldHaveErrorWhenSubtractingBooleans() {
        useExpression("true - false")
        assertTrue(binder.hasErrors())
    }

    @Test
    fun shouldHaveErrorWhenDividingBooleans() {
        useExpression("true / false")
        assertTrue(binder.hasErrors())
    }

    @Test
    fun shouldHaveErrorWhenMultiplyingBooleanWithNumber() {
        useExpression("true * 1")
        assertTrue(binder.hasErrors())
    }

    @Test
    fun shouldNotHaveErrorWhenMultiplyingNumbers() {
        useExpression("1 * 2")
        assertTrue(!binder.hasErrors())
    }

    @Test
    fun shouldHaveErrorWhenUsingBangOperatorOnNonBoolean() {
        useExpression("not 1")
        assertTrue(binder.hasErrors())
    }

    @Test
    fun shouldNotHaveErrorWhenUsingBangOperatorOnBoolean() {
        useExpression("not true")
        assertTrue(!binder.hasErrors())
    }

    @Test
    fun shouldHaveErrorsWhenUsingLessThanOnNonNumbers() {
        useExpression("true < false")
        assertTrue(binder.hasErrors())
    }

    @Test
    fun shouldNotHaveErrorsWhenUsingLessThanOnNumbers() {
        useExpression("1 < 2")
        assertTrue(!binder.hasErrors())
    }

    @Test
    fun shouldHaveErrorsWhenUsingGreaterThanOnNonNumbers() {
        useExpression("true > false")
        assertTrue(binder.hasErrors())
    }

    @Test
    fun shouldNotHaveErrorsWhenUsingGreaterThanOnNumbers() {
        useExpression("1 > 2")
        assertTrue(!binder.hasErrors())
    }

    @Test
    fun shouldNotHaveErrorsWhenUsingGreaterThanOrEqualsOnNumbers() {
        useExpression("1 >= 2")
        assertTrue(!binder.hasErrors())
    }

    @Test
    fun shouldNotHaveErrorsWhenUsingLessThanOrEqualsOnNumbers() {
        useExpression("1 <= 2")
        assertTrue(!binder.hasErrors())
    }

    @Test
    fun shouldNotHaveErrorsWhenUsingEqualsOnNumbers() {
        useExpression("1 == 2")
        assertTrue(!binder.hasErrors())
    }

    @Test
    fun shouldHaveErrorsWhenUsingEqualsOnDifferentTypes() {
        useExpression("1 == true")
        assertTrue(binder.hasErrors())
    }

    @Test
    fun shouldHaveErrorWhenUsingLessThanOrEqualsOnNonNumbers() {
        useExpression("true <= false")
        assertTrue(binder.hasErrors())
    }

    @Test
    fun shouldHaveErrorWhenUsingGreaterThanOrEqualsOnNonNumbers() {
        useExpression("true >= false")
        assertTrue(binder.hasErrors())
    }

    @Test
    fun shouldHaveErrorWhenUsingPowOperatorOnNonNumbers() {
        useExpression("true ^^ false")
        assertTrue(binder.hasErrors())
    }

    @Test
    fun shouldNotHaveErrorWhenUsingPowOperatorOnNumbers() {
        useExpression("1 ^^ 2")
        assertTrue(!binder.hasErrors())
    }

    @Test
    fun xorShouldNotHaveErrorWhenUsingOnBooleans() {
        useExpression("true xor false")
        assertTrue(!binder.hasErrors())
    }

    @Test
    fun xorShouldHaveErrorWhenUsingOnNonBooleans() {
        useExpression("1 xor 2")
        assertTrue(binder.hasErrors())
    }

    @Test
    fun bitwiseXorShouldNotHaveErrorWhenUsingOnNumbers() {
        useExpression("1 ^ 2")
        assertTrue(!binder.hasErrors())
    }

    @Test
    fun bitwiseXorShouldHaveErrorWhenUsingOnNonNumbers() {
        useExpression("true ^ false")
        assertTrue(binder.hasErrors())
    }

    @Test
    fun leftShiftShouldNotHaveErrorWhenUsingOnNumbers() {
        useExpression("1 << 2")
        assertTrue(!binder.hasErrors())
    }

    @Test
    fun leftShiftShouldHaveErrorWhenUsingOnNonNumbers() {
        useExpression("true << false")
        assertTrue(binder.hasErrors())
    }

    @Test
    fun rightShiftShouldNotHaveErrorWhenUsingOnNumbers() {
        useExpression("1 >> 2")
        assertTrue(!binder.hasErrors())
    }

    @Test
    fun rightShiftShouldHaveErrorWhenUsingOnNonNumbers() {
        useExpression("true >> false")
        assertTrue(binder.hasErrors())
    }


    @Test
    fun shouldHaveErrorWhenUsingIsOperatorOnValueAndValue() {
        useExpression("1 is 2")
        assertTrue(binder.hasErrors())
    }

    @Test
    fun shouldNotHaveErrorWhenUsingIsOperatorOnValueAndType() {
        useExpression("1 is Int")
        assertTrue(!binder.hasErrors())
    }

    @Test
    fun isShouldHaveErrorWhenUsedOnNonExistentType() {
        useExpression("1 is NonExistentType")
        assertTrue(binder.hasErrors())
    }

    @Test
    fun shouldAllowAssignmentToIdentifier() {
        useExpression("$TEST_VARIABLE_NAME = 1")
        assertTrue(!binder.hasErrors())
    }

    @Test
    fun shouldNotAllowAssignmentToLiteral() {
        useExpression("1 = 1")
        assertTrue(binder.hasErrors())
    }

    @Test
    fun shouldAllowAssignmentToDeclaredVariable() {
        useExpression("$TEST_VARIABLE_NAME = 2")
        assertTrue(!binder.hasErrors())
    }


    @Test
    fun shouldNotAllowAssignmentToOtherType() {
        useExpression("$TEST_VARIABLE_NAME = true")
        assertTrue(binder.hasErrors())
    }

    @Test
    fun shouldAllowTypeOfOnValue() {
        useExpression("typeof 1")
        assertTrue(!binder.hasErrors())
    }

    @Test
    fun shouldAllowTypeOfOnType() {
        useExpression("typeof Int")
        assertTrue(!binder.hasErrors())
    }

    @Test
    fun shouldAllowTypeofOnVariable() {
        useExpression("typeof $TEST_VARIABLE_NAME")
        assertTrue(!binder.hasErrors())
    }

    @Test
    fun shouldAllowTypeComparison() {
        useExpression("typeof $TEST_VARIABLE_NAME == Int")
        assertTrue(!binder.hasErrors())
    }
}