package lang.proteus.binder

import lang.proteus.binding.Binder
import lang.proteus.binding.BoundScope
import lang.proteus.binding.ProteusType
import lang.proteus.symbols.VariableSymbol
import lang.proteus.syntax.parser.Parser
import lang.proteus.syntax.parser.statements.StatementSyntax
import lang.proteus.text.SourceText
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class BinderTest {

    private lateinit var binder: Binder

    companion object {
        private const val TEST_VARIABLE_NAME = "x"
        private const val TEST_VARIABLE_VALUE = 1
    }

    private fun useExpression(input: String) {
        val expression = parseExpression(input)
        val variables: MutableMap<String, Any> = mutableMapOf(
            TEST_VARIABLE_NAME to TEST_VARIABLE_VALUE
        )
        val scope = BoundScope(null)
        scope.tryDeclare(VariableSymbol(TEST_VARIABLE_NAME, ProteusType.Int, isFinal = false))
        binder = Binder(scope)
        binder.bindStatement(expression)
    }

    private fun parseExpression(input: String): StatementSyntax {
        val parser = Parser(SourceText.from(input))
        val compilationUnitSyntax = parser.parseCompilationUnit()
        return compilationUnitSyntax.statement
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
        useExpression("true ** false")
        assertTrue(binder.hasErrors())
    }

    @Test
    fun shouldNotHaveErrorWhenUsingPowOperatorOnNumbers() {
        useExpression("1 ** 2")
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

    @Test
    fun shouldNotAllowAssignmentToNonExistentVariable() {
        useExpression("nonExistentVariable = 1")
        assertTrue(binder.hasErrors())
    }

    @Test
    fun shouldAllowAssignmentToVariableInScope() {
        useExpression(
            """
            var a = 1
            a = 2
        """.trimIndent()
        )
        assertTrue(!binder.hasErrors())
    }

    @Test
    fun shouldNotAllowAssignmentToImmutableVariableInScope() {
        useExpression(
            """
                {
            val a = 1
            a = 2
            }
        """.trimIndent()
        )
        assertTrue(binder.hasErrors())
    }

    @Test
    fun shouldAllowAssignmentToVariableInParentScope() {
        useExpression(
            """
                {
            var a = 1
            {
                a = 2
            }
            }
        """.trimIndent()
        )
        assertTrue(!binder.hasErrors())
    }

    @Test
    fun shouldNotAllowAssignmentToImmutableVariableInParentScope() {
        useExpression(
            """
            {
    val a = 1
    {
        a = 2
    }
}
        """.trimIndent()
        )
        assertTrue(binder.hasErrors())
    }

    @Test
    fun shouldAllowAssignmentToVariableInGrandparentScope() {
        useExpression(
            """
                {
            var a = 1
            {
                {
                    a = 2
                }
            }
            }
        """.trimIndent()
        )
        assertTrue(!binder.hasErrors())
    }

    @Test
    fun shouldNotAllowAssignmentToImmutableVariableInGrandparentScope() {
        useExpression(
            """
                {
            val a = 1
            {
                {
                    a = 2
                }
            }
            }
        """.trimIndent()
        )
        assertTrue(binder.hasErrors())
    }


    @Test
    fun `should not allow duplicate declaration`() {
        useExpression("""
            {
                val a = 1
                {
                    var x = 2
                    val x = 2
                }
            }
        """.trimIndent())
        assertTrue(binder.hasErrors())
    }

    @Test
    fun shouldAllowAssignmentToVariableInChildScope() {
        useExpression(
            """
            var a = 1
            {
                a = 2
            }
        """.trimIndent()
        )
        assertTrue(!binder.hasErrors())
    }
}