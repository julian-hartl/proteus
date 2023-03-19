package lang.proteus.binder

import lang.proteus.binding.Binder
import lang.proteus.binding.BoundScope
import lang.proteus.syntax.parser.*
import lang.proteus.syntax.parser.statements.StatementSyntax
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class BinderTest {

    private lateinit var binder: Binder


    private fun useExpression(input: String, inMain: Boolean = true) {
        val parseResult = parseExpression(
            if (inMain) {
                "fn main() { $input }"
            } else {
                input
            }
        )
        val scope = BoundScope(null)

        binder = Binder(scope, null, null)
        binder.bindStatement(parseResult.expression)
    }

    private data class ParseExpressionResult(
        val expression: StatementSyntax,
        val tree: SyntaxTree,
    )

    private fun parseExpression(input: String): ParseExpressionResult {
        val parser = Parser(input)
        val compilationUnitSyntax = parser.parseCompilationUnit()
        val member = compilationUnitSyntax.members[0]
        val statement = when (member) {
            is GlobalVariableDeclarationSyntax -> member.statement
            is FunctionDeclarationSyntax -> member.body!!
            is ImportStatementSyntax -> TODO()
            is StructDeclarationSyntax -> TODO()
        }
        return ParseExpressionResult(statement, compilationUnitSyntax.syntaxTree)
    }

    private fun assertHasNoErrors() {
        assertTrue(!binder.hasErrors(), "Expected no errors, but got ${binder.diagnostics}")
    }

    private fun assertHasErrors() {
        assertTrue(binder.hasErrors(), "Expected errors, but got none")
    }

    @Test
    fun shouldHaveErrorWhenMultiplyingBooleans() {
        useExpression("true * false")
        assertHasErrors()
    }

    @Test
    fun shouldHaveErrorWhenAddingBooleans() {
        useExpression("true + false")
        assertHasErrors()
    }

    @Test
    fun shouldHaveErrorWhenSubtractingBooleans() {
        useExpression("true - false")
        assertHasErrors()
    }

    @Test
    fun shouldHaveErrorWhenDividingBooleans() {
        useExpression("true / false")
        assertHasErrors()
    }

    @Test
    fun shouldHaveErrorWhenMultiplyingBooleanWithNumber() {
        useExpression("true * 1")
        assertHasErrors()
    }

    @Test
    fun shouldNotHaveErrorWhenMultiplyingNumbers() {
        useExpression("1 * 2")
        assertHasNoErrors()
    }

    @Test
    fun shouldHaveErrorWhenUsingBangOperatorOnNonBoolean() {
        useExpression("not 1")
        assertHasErrors()
    }

    @Test
    fun shouldNotHaveErrorWhenUsingBangOperatorOnBoolean() {
        useExpression("not true")
        assertHasNoErrors()
    }

    @Test
    fun shouldHaveErrorsWhenUsingLessThanOnNonNumbers() {
        useExpression("true < false")
        assertHasErrors()
    }

    @Test
    fun shouldNotHaveErrorsWhenUsingLessThanOnNumbers() {
        useExpression("1 < 2")
        assertHasNoErrors()
    }

    @Test
    fun shouldHaveErrorsWhenUsingGreaterThanOnNonNumbers() {
        useExpression("true > false")
        assertHasErrors()
    }

    @Test
    fun shouldNotHaveErrorsWhenUsingGreaterThanOnNumbers() {
        useExpression("1 > 2")
        assertHasNoErrors()
    }

    @Test
    fun shouldNotHaveErrorsWhenUsingGreaterThanOrEqualsOnNumbers() {
        useExpression("1 >= 2")
        assertHasNoErrors()
    }

    @Test
    fun shouldNotHaveErrorsWhenUsingLessThanOrEqualsOnNumbers() {
        useExpression("1 <= 2")
        assertHasNoErrors()
    }

    @Test
    fun shouldNotHaveErrorsWhenUsingEqualsOnNumbers() {
        useExpression("1 == 2")
        assertHasNoErrors()
    }

    @Test
    fun shouldHaveErrorsWhenUsingEqualsOnDifferentTypes() {
        useExpression("1 == true")
        assertHasErrors()
    }

    @Test
    fun shouldHaveErrorWhenUsingLessThanOrEqualsOnNonNumbers() {
        useExpression("true <= false")
        assertHasErrors()
    }

    @Test
    fun shouldHaveErrorWhenUsingGreaterThanOrEqualsOnNonNumbers() {
        useExpression("true >= false")
        assertHasErrors()
    }

    @Test
    fun shouldHaveErrorWhenUsingPowOperatorOnNonNumbers() {
        useExpression("true ** false")
        assertHasErrors()
    }

    @Test
    fun shouldNotHaveErrorWhenUsingPowOperatorOnNumbers() {
        useExpression("1 ** 2")
        assertHasNoErrors()
    }

    @Test
    fun xorShouldNotHaveErrorWhenUsingOnBooleans() {
        useExpression("true xor false")
        assertHasNoErrors()
    }

    @Test
    fun xorShouldHaveErrorWhenUsingOnNonBooleans() {
        useExpression("1 xor 2")
        assertHasErrors()
    }

    @Test
    fun bitwiseXorShouldNotHaveErrorWhenUsingOnNumbers() {
        useExpression("1 ^ 2")
        assertHasNoErrors()
    }

    @Test
    fun bitwiseXorShouldHaveErrorWhenUsingOnNonNumbers() {
        useExpression("true ^ false")
        assertHasErrors()
    }

    @Test
    fun leftShiftShouldNotHaveErrorWhenUsingOnNumbers() {
        useExpression("1 << 2")
        assertHasNoErrors()
    }

    @Test
    fun leftShiftShouldHaveErrorWhenUsingOnNonNumbers() {
        useExpression("true << false")
        assertHasErrors()
    }

    @Test
    fun rightShiftShouldNotHaveErrorWhenUsingOnNumbers() {
        useExpression("1 >> 2")
        assertHasNoErrors()
    }

    @Test
    fun rightShiftShouldHaveErrorWhenUsingOnNonNumbers() {
        useExpression("true >> false")
        assertHasErrors()
    }


    @Test
    fun shouldHaveErrorWhenUsingIsOperatorOnValueAndValue() {
        useExpression("1 is 2")
        assertHasErrors()
    }

    @Test
    fun shouldNotHaveErrorWhenUsingIsOperatorOnValueAndType() {
        useExpression("1 is Int")
        assertHasNoErrors()
    }

    @Test
    fun isShouldHaveErrorWhenUsedOnNonExistentType() {
        useExpression("1 is NonExistentType")
        assertHasErrors()
    }


    @Test
    fun shouldNotAllowAssignmentToLiteral() {
        useExpression("1 = 1")
        assertHasErrors()
    }

    @Test
    fun shouldAllowAssignmentToDeclaredVariable() {
        useExpression(
            """
            var a = 1
            a = 2
        """.trimIndent()
        )
        assertHasNoErrors()
    }


    @Test
    fun shouldNotAllowAssignmentToOtherType() {
        useExpression(
            """
            var a = 1
            a = true
        """.trimIndent()
        )
        assertHasErrors()
    }

    @Test
    fun shouldAllowTypeOfOnValue() {
        useExpression("typeof 1")
        assertHasNoErrors()
    }

    @Test
    fun shouldAllowTypeOfOnType() {
        useExpression("typeof Int")
        assertHasNoErrors()
    }

    @Test
    fun shouldAllowTypeofOnVariable() {
        useExpression(
            """
            var a = 1;
            typeof a;
        """.trimIndent()
        )
        assertHasNoErrors()
    }

    @Test
    fun shouldAllowTypeComparison() {
        useExpression(
            """
            var a = 1;
            typeof a == typeof 2;
        """.trimIndent()
        )
        assertHasNoErrors()
    }

    @Test
    fun shouldNotAllowAssignmentToNonExistentVariable() {
        useExpression("nonExistentVariable = 1")
        assertHasErrors()
    }

    @Test
    fun shouldAllowAssignmentToVariableInScope() {
        useExpression(
            """
            var a = 1
            a = 2
        """.trimIndent()
        )
        assertHasNoErrors()
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
        assertHasErrors()
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
        assertHasNoErrors()
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
        assertHasErrors()
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
        assertHasNoErrors()
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
        assertHasErrors()
    }


    @Test
    fun `should not allow duplicate declaration`() {
        useExpression(
            """
            {
                val a = 1
                {
                    var x = 2
                    val x = 2
                }
            }
        """.trimIndent()
        )
        assertHasErrors()
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
        assertHasNoErrors()
    }

    @Test
    fun shouldNotAllowPlusEqualsAssignmentWithWrongType() {
        useExpression(
            """
            {
                var a = 1
                a += true
            }
        """.trimIndent()
        )
        assertHasErrors()
    }

    @Test
    fun shouldNotAllowMinusEqualsAssignmentWithWrongType() {
        useExpression(
            """
            {
                var a = 1
                a -= true
            }
        """.trimIndent()
        )
        assertHasErrors()
    }

    @Test
    fun shouldReportErrorWhenUsingFunctionWithWrongNumberOfArguments() {
        useExpression("print();")
        assertHasErrors()
    }

    @Test
    fun shouldReportErrorWhenNonConstantIsUsedInConstantDeclaration() {
        useExpression(
            """
            var b = 20;
            const a = 2 * b;
        """.trimIndent(),
            inMain = true
        )
        assertHasErrors()
    }

    @Test
    fun shouldNotReportErrorWhenConstantIsUsedInConstantDeclaration() {
        useExpression(
            """
            const a = 20;
            const b = 2 * a;
        """.trimIndent(),
            inMain = true
        )
        println(binder.diagnostics)
        assertHasNoErrors()
    }

    @Test
    fun shouldDetectDeadCode() {
        useExpression(
            """
            fn main() -> Int {
                return 1;
                val a = 2;
            }
            
        """.trimIndent()
        )
        assertHasErrors()
    }


}