package lang.proteus.evaluation

import lang.proteus.api.Compilation
import lang.proteus.binding.ProteusType
import lang.proteus.syntax.parser.SyntaxTree
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class EvaluationTest {
    @ParameterizedTest
    @MethodSource("getValidSyntaxInputs")
    fun `test valid inputs to evaluate correctly`(input: String, value: Any) {
        val expression = SyntaxTree.parse(input)
        val compilation = Compilation(expression)
        val variables: Map<String, Any> = mapOf(
            "a" to 42,
            "b" to -4,
        )
        val evaluationResult = compilation.evaluate(variables)
        assertFalse(
            evaluationResult.diagnostics.hasErrors(),
            "Compilation should not have errors, but it has: ${evaluationResult.diagnostics}"
        )
        assertEquals(
            value,
            evaluationResult.value,
            "Evaluation result should be $value, but was ${evaluationResult.value}"
        )
    }

    companion object {
        @JvmStatic
        fun getValidSyntaxInputs(): List<Arguments> {
            return listOf(
                Arguments.of("2", 2),
                Arguments.of("(2)", 2),
                Arguments.of("+2", 2),
                Arguments.of("-2", -2),
                Arguments.of("2 + 2", 4),
                Arguments.of("2 - 2", 0),
                Arguments.of("2 * 2", 4),
                Arguments.of("2 / 2", 1),
                Arguments.of("2 ^^ 2", 4),
                Arguments.of("2 + 2 * 2", 6),
                Arguments.of("2 * (2 + 2)", 8),
                Arguments.of("2 * (2 + 2) / 2", 4),

                Arguments.of("2 & 2", 2),
                Arguments.of("2 | 2", 2),
                Arguments.of("2 ^ 2", 0),
                Arguments.of("2 & 2 | 2", 2),

                Arguments.of("true", true),
                Arguments.of("false", false),
                Arguments.of("true and true", true),
                Arguments.of("true and false", false),
                Arguments.of("false and true", false),
                Arguments.of("false and false", false),
                Arguments.of("true or true", true),
                Arguments.of("true or false", true),
                Arguments.of("false or true", true),
                Arguments.of("false or false", false),
                Arguments.of("not true", false),
                Arguments.of("not false", true),
                Arguments.of("true and true or false", true),
                Arguments.of("true and (true or false)", true),
                Arguments.of("true and (true or false) and true", true),
                Arguments.of("true and (true or false) and false", false),
                Arguments.of("true and (true or false) and not false", true),
                Arguments.of("true and (true or false) and not false or false", true),
                Arguments.of("true xor true", false),
                Arguments.of("true xor false", true),
                Arguments.of("false xor true", true),
                Arguments.of("false xor false", false),
                Arguments.of("(true) xor (true)", false),
                Arguments.of("(true) xor (false)", true),
                Arguments.of("false == false", true),
                Arguments.of("false == true", false),
                Arguments.of("true == false", false),
                Arguments.of("true == true", true),
                Arguments.of("false != false", false),
                Arguments.of("false != true", true),
                Arguments.of("true != false", true),
                Arguments.of("true != true", false),
                Arguments.of("true == true and false == false", true),
                Arguments.of("true == true and false == false or false == true", true),
                Arguments.of("1 == 1", true),
                Arguments.of("1 == 2", false),
                Arguments.of("1 != 1", false),
                Arguments.of("1 != 2", true),

                Arguments.of("1 < 2", true),
                Arguments.of("1 < 1", false),
                Arguments.of("2 < 1", false),
                Arguments.of("1 <= 2", true),
                Arguments.of("1 <= 1", true),
                Arguments.of("2 <= 1", false),
                Arguments.of("1 > 2", false),
                Arguments.of("1 > 1", false),
                Arguments.of("2 > 1", true),
                Arguments.of("1 >= 2", false),
                Arguments.of("1 >= 1", true),
                Arguments.of("2 + 1 >= 1", true),
                Arguments.of("2 >= 1", true),
                Arguments.of("1 >= 2", false),
                Arguments.of("1 >= 1", true),
                Arguments.of("2 + 1 >= 1", true),
                Arguments.of("( 2 * 2 ) > 3", true),

                Arguments.of("typeof 2", ProteusType.Int),
                Arguments.of("typeof true", ProteusType.Boolean),
                Arguments.of("typeof false == Boolean", true),
                Arguments.of("typeof false == Int", false),

                Arguments.of("1 is Int", true),
                Arguments.of("1 is Boolean", false),
                Arguments.of("1 is Int and 1 is Boolean", false),
                Arguments.of("1 is Int or 1 is Boolean", true),

                Arguments.of("(a = 10) * 10", 100),
                Arguments.of("a", 42),
                Arguments.of("b", -4),
                Arguments.of("a + b", 38),
            )
        }
    }
}