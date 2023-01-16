package lang.proteus.evaluation

import lang.proteus.api.Compilation
import lang.proteus.api.ProteusCompiler
import lang.proteus.binding.*
import lang.proteus.symbols.VariableSymbol
import lang.proteus.syntax.parser.SyntaxTree
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class EvaluationTest {


    @Test
    fun `evaluator BlockStatement NoInfiniteLoop`() {
        val text = """
            {([]
        """

        ProteusCompiler().compile(text)
    }

    @ParameterizedTest
    @MethodSource("getValidSyntaxInputs")
    fun `test valid inputs to evaluate correctly`(input: String, value: Any) {
        val expression = SyntaxTree.parse(input)
        val boundScope = BoundScope(null)
        boundScope.tryDeclare(VariableSymbol("a", ProteusType.Int, isFinal = false))
        boundScope.tryDeclare(VariableSymbol("b", ProteusType.Int, isFinal = false))
        val compilation = Compilation(expression)

        val evaluationResult = compilation.evaluate(mutableMapOf())
        assertFalse(
            evaluationResult.diagnostics.hasErrors(),
            "Compilation should not have errors, but it has: ${evaluationResult.diagnostics}"
        )
        assertEquals(
            value,
            evaluationResult.value,
            "Evaluation result should be $value, but was $evaluationResult"
        )
    }

    @ParameterizedTest
    @MethodSource("getBlockStatements")
    fun `should value from last statement in block`(input: String, expectedValue: Any) {
        assertValue(input, expectedValue)
    }

    private fun assertValue(input: String, expectedValue: Any) {
        val compiler = ProteusCompiler()
        val result = compiler.compile(input)
        assertEquals(
            expectedValue,
            result.evaluationResult?.value,
            "Evaluation result should be $expectedValue, but was ${result.evaluationResult?.value}"
        )
    }

    companion object {

        @JvmStatic
        val blockStatements: Stream<Arguments> = Stream.of(
            Arguments.of(
                """
                {
                    1;
                    2;
                    3;
                }
            """.trimIndent(), 3
            ),
            Arguments.of(
                """
                {
                    1;
                    5 * 5;
                }
            """.trimIndent(), 25
            ),
        )

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
                Arguments.of("2 ** 3", 8),
                Arguments.of("2 + 2 * 2", 6),
                Arguments.of("2 * (2 + 2)", 8),
                Arguments.of("2 * (2 + 2) / 2", 4),
                Arguments.of("7 % 2", 1),

                Arguments.of("1 & 2", 0),
                Arguments.of("2 | 2", 2),
                Arguments.of("1 | 2", 3),
                Arguments.of("2 ^ 2", 0),
                Arguments.of("2 & 2 | 2", 2),
                Arguments.of("2 << 2", 8),
                Arguments.of("8 >> 2", 2),

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
                Arguments.of("typeof \"test\"", ProteusType.String),
                Arguments.of("typeof 't'", ProteusType.Char),
                Arguments.of("typeof false == Boolean", true),
                Arguments.of("typeof false == Int", false),


                Arguments.of("1 is Int", true),
                Arguments.of("1 is Boolean", false),
                Arguments.of("1 is Int and 1 is Boolean", false),
                Arguments.of("1 is Int or 1 is Boolean", true),

                Arguments.of("{ var a = 0; a = (a = 10) * 10; }", 100),
                Arguments.of("{ val a = 42; a;}", 42),
                Arguments.of("{ val b = -4; b;}", -4),
                Arguments.of("""
                    {
                        val a = 42;
                        val b = a + 1;
                        b;
                    }
                """.trimIndent(), 43),

                Arguments.of(
                    """
                    {
                        var a = 0;
                        a += 10;
                        a;
                    }
                """.trimIndent(), 10
                ),

                Arguments.of(
                    """
                    {
                        var a = 0;
                        a -= 10;
                        a;
                    }
                """.trimIndent(), -10
                ),

                Arguments.of(
                    """
                        {
                            var x = 1 + 2;
                            typeof x;
                        }

                    """.trimIndent(),
                    ProteusType.Int
                ),

                Arguments.of(
                    """
                    {
                       var x = 10;
                       if x == 10
                            x = 20;
                       x;
                    }
                """.trimIndent(), 20
                ),

                Arguments.of(
                    """
                    {
                        var x = 10;
                        if x == 20 {
                            x = 5;
                        }
                        else {
                            x = 7;
                        }
                        x;
                    }
                """.trimIndent(), 7
                ),

                Arguments.of(
                    """
                    {
                        var x = 10;
                        while x > 0
                            x = x - 1;
                        x;
                    }
                """.trimIndent(), 0
                ),

                Arguments.of(
                    """
                    {
                        var b = 0;
                        for x in 1 until 10 {
                            b = b + x;
                        }
                        b;
                    }
                """.trimIndent(), 55
                ),

                Arguments.of(
                    """
                        {
                            var b = 0;
                            if b == 0
                                b = 10;
                            b;
                        }
                    """.trimIndent(), 10
                ),

                Arguments.of(
                    """
                        {
                            var b = 10;
                            for x in 1 until 10 {
                                b += x;
                            }
                            b;
                        }
                    """.trimIndent(), 65
                ),

                Arguments.of(
                    """
                       {
                            var b = 0;
                            if (b == 0) {
                                b = 10;
                            };
                            b;
                        }
                    """.trimIndent(),
                    10
                ),

                Arguments.of(
                    """
                        {
                            val test = "{ val test = a; }";
                            var a = 0;
                            if test == "{ val test = a; }" {
                                a = 10;
                            } else {
                                a = 2;
                            }
                            a;
                        }
                    """.trimIndent(),
                    10
                )
            )
        }
    }

    @Test
    fun `evaluator variable declaration reports redeclaration`() {
        val text = """
            {
                var x = 10;
                var y = 100;
                {
                    var x = 10;
                }
                var [x] = 5;
            }
        """

        val diagnostics = "Variable 'x' already declared"
        assertDiagnostics(text, diagnostics)
    }

    @Test
    fun `evaluator NameExpression reports undefined`() {
        val text = """
            [x] * 10
        """

        val diagnostics = "Unresolved reference: x"
        assertDiagnostics(text, diagnostics)
    }

    @Test
    fun `evaluator Assignment reports unresolved reference`() {
        val text = """
            [x] = 10
        """

        val diagnostics = "Unresolved reference: x"
        assertDiagnostics(text, diagnostics)
    }

    @Test
    fun `evaluator Assignment reports cannot be reassigned`() {
        val text = """
            {
                val x = 10;
                [x] = 20;
            }
        """
        val diagnostics = "Val cannot be reassigned"
        assertDiagnostics(text, diagnostics)
    }

    @Test
    fun `evaluator Assignment reports cannot convert`() {
        val text = """
            {
                var x = 10;
                x = [false];
            }
        """
        val diagnostics = "Cannot convert type 'Boolean' to 'Int'"
        assertDiagnostics(text, diagnostics)
    }

    @Test
    fun `evaluator Binary reports undefined operator`() {
        val text = """
            {
                var x = 5 [+] false;
            }
        """
        val diagnostics = "Operator '+' is not defined for types 'Int' and 'Boolean'"
        assertDiagnostics(text, diagnostics)
    }

    @Test
    fun `evaluator Unary reports undefined operator`() {
        val text = """
            {
                var x = [+]false;
            }
        """
        val diagnostics = "Operator '+' is not defined for type 'Boolean'"
        assertDiagnostics(text, diagnostics)
    }

    @Test
    fun `evaluator If reports not Boolean condition`() {
        val text = """
            {
                if [10] {
                    var x = 10;
                }
            }
        """
        val diagnostics = "Cannot convert type 'Int' to 'Boolean'"
        assertDiagnostics(text, diagnostics)
    }

    @Test
    fun `evaluator While reports not Boolean condition`() {
        val text = """
            {
                while [10] {
                    var x = 10;
                }
            }
        """
        val diagnostics = "Cannot convert type 'Int' to 'Boolean'"
        assertDiagnostics(text, diagnostics)
    }

    @Test
    fun `evaluator range operator reports not Integer in lower bound`() {
        val text = """
            {
                for i in [false] until 20 {
                }
            }
        """
        val diagnostics = """
            Cannot convert type 'Boolean' to 'Int'
        """.trimIndent()
        assertDiagnostics(text, diagnostics)
    }

    @Test
    fun `evaluator range operator reports not Integer in upper bound`() {
        val text = """
            {
                for i in 10 until [true] {
                }
            }
        """
        val diagnostics = """
            Cannot convert type 'Boolean' to 'Int'
        """.trimMargin()
        assertDiagnostics(text, diagnostics)
    }

    @Test
    fun `evaluator NameExpression reports no error for inserted token`() {
        val text = """
            [][]
        """
        val diagnostics = """
            Unexpected token <EndOfFile>, expected <Expression>
            Unexpected token <EndOfFile>, expected <Identifier>
        """.trimIndent()
        assertDiagnostics(text, diagnostics)
    }

    @Test
    fun `evaluator final variable should not be reassigned with += operator`() {
        val text = """
            {
                val a = 2;
                [a] += 1;
            }
        """.trimIndent()
        val diagnostics = "Val cannot be reassigned"

        assertDiagnostics(text, diagnostics)
    }

    @Test
    fun `evaluator final variable should not be reassigned with -= operator`() {
        val text = """
            {
                val a = 2;
                [a] -= 1;
            }
        """.trimIndent()
        val diagnostics = "Val cannot be reassigned"

        assertDiagnostics(text, diagnostics)
    }


    @Test
    fun `evaluator += operator should not be applicable to Boolean`() {
        val text = """
            {
                var a = true;
                a += [1];
            }
        """.trimIndent()
        val diagnostics = "Cannot convert type 'Int' to 'Boolean'"

        assertDiagnostics(text, diagnostics)
    }

    @Test
    fun `evaluator -= operator should not be applicable to Boolean`() {
        val text = """
            {
                var a = true;
                a -= [1];
            }
        """.trimIndent()
        val diagnostics = "Cannot convert type 'Int' to 'Boolean'"

        assertDiagnostics(text, diagnostics)
    }

    private fun assertDiagnostics(text: String, diagnosticText: String) {
        val annotatedText = AnnotatedText.parse(text)
        val syntaxTree = SyntaxTree.parse(annotatedText.text)
        val compilation = Compilation(syntaxTree)
        val result = compilation.evaluate(mutableMapOf())

        val expectedDiagnostics = AnnotatedText.unindentLines(diagnosticText)

        if (annotatedText.spans.size != expectedDiagnostics.size) {
            throw IllegalArgumentException("Number of diagnostics does not match number of spans.")
        }

        assertEquals(
            expectedDiagnostics.size,
            result.diagnostics.diagnostics.size,
            "Did not get the expected number of diagnostics, actual: ${result.diagnostics.diagnostics}."
        )

        var assertedDiagnostics = 0
        for (i in expectedDiagnostics.indices) {
            val expectedMessage = expectedDiagnostics[assertedDiagnostics]
            val actualMessage = result.diagnostics.diagnostics[i].message
            assertEquals(expectedMessage, actualMessage, "Diagnostic message does not match.")

            val expectedSpan = annotatedText.spans[i]
            val actualSpan = result.diagnostics.diagnostics[i].span
            assertEquals(expectedSpan, actualSpan, "Diagnostic span does not match: ${result.diagnostics.diagnostics}")

            assertedDiagnostics++
        }
        assertEquals(
            expectedDiagnostics.size, assertedDiagnostics, """
            Did not get the expected amount diagnostics.
            Expected: $expectedDiagnostics
            Actual: ${result.diagnostics.diagnostics}
        """.trimIndent()
        )
    }
}