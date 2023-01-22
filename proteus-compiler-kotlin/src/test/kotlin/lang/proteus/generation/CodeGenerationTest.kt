package lang.proteus.generation

import lang.proteus.api.Compilation
import lang.proteus.syntax.parser.SyntaxTree
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertEquals

class CodeGenerationTest {
    private fun useProgram(input: String): String {
        val parsed = SyntaxTree.parse(input)
        val compilation = Compilation(parsed)
        val code = compilation.evaluate(mutableMapOf()).generatedCode
        return code!!
    }

    @ParameterizedTest
    @MethodSource
    fun `should generate correct code`(input: String, expected: String) {
        val code = useProgram(input)
        assertEquals(expected, code, "Generated code is not correct")
    }

    companion object {
        @JvmStatic
        fun `should generate correct code`(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    """
                        fn main() {
                            val a = 1;
                            val b = 2;
                        }
                        main();
                    """.trimIndent(),
                    """
                        fn main() -> Unit {
                            var a = 1;
                            var b = 2;
                        }
                        {
                            main();
                        }
                        
                    """.trimIndent()
                ),
                Arguments.of(
                    """
                        fn main() {
                            val a = 1;
                            val b = 2;
                            val c = a + b;
                        }
                        main();
                    """.trimIndent(),
                    """
                        fn main() -> Unit {
                            var a = 1;
                            var b = 2;
                            var c = (a + b);
                        }
                        {
                            main();
                        }
                        
                    """.trimIndent()
                ),
                Arguments.of(
                    """
                        fn main() {
                        if (true) {
                            val a = 1;
                            val b = 2;
                            val c = a + b;
                            }
                            else {
                            val a = 1;
                            }
                        }
                    """.trimIndent(),
                    """
                        fn main() -> Unit {
                            gotoIfFalse label0 true;
                            var a = 1;
                            var b = 2;
                            var c = (a + b);
                            goto label1;
                            label0:
                            var a = 1;
                            label1:
                        }
                        {
                        }
                        
                    """.trimIndent()
                )
            )
        }
    }


}