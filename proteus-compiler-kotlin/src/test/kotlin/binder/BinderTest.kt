package binder

import binding.Binder
import org.junit.jupiter.api.Test
import syntax.parser.ExpressionSyntax
import syntax.parser.Parser
import kotlin.test.assertTrue

class BinderTest {

    private lateinit var binder: Binder

    private fun useExpression(input: String) {
        val expression = parseExpression(input)
        binder = Binder()
        binder.bind(expression)
    }

    private fun parseExpression(input: String): ExpressionSyntax {
        val parser = Parser(input)
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


}