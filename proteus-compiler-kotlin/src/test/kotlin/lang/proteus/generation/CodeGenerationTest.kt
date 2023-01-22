package lang.proteus.generation

import lang.proteus.binding.Binder
import lang.proteus.syntax.parser.SyntaxTree
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertEquals

class CodeGenerationTest {
    private fun useProgram(input: String): String {
        val parsed = SyntaxTree.parse(input)
        val globalScope = Binder.bindGlobalScope(null, parsed.root)
        val program = Binder.bindProgram(globalScope, optimize = false)
        return CodeGenerator.generate(program.globalScope.statement, program.functionBodies)
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
                            var c = ((a) + (b));
                        }
                        {
                            main();
                        }
                        
                    """.trimIndent()
                ),
                Arguments.of(
                    """
                        fn main() {
                            val test = "test";
                        }
                    """.trimIndent(),
                    """
                        fn main() -> Unit {
                            var test = "test";
                        }
                        {
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
                            gotoIfFalse else_0 true;
                            var a = 1;
                            var b = 2;
                            var c = ((a) + (b));
                            goto if_0_end;
                            else_0:
                            var a = 1;
                            if_0_end:
                        }
                        {
                        }
                        
                    """.trimIndent()
                ),

                Arguments.of(
                    """
                        fn main() {
                            return;
                        }
                    """.trimIndent(),
                    """
                        fn main() -> Unit {
                            return;
                        }
                        {
                        }
                        
                    """.trimIndent()
                ),
                Arguments.of(
                    """
                        fn main() -> Int{
                            return 1 + 1;
                        }
                    """.trimIndent(),
                    """
                        fn main() -> Int {
                            return ((1) + (1));
                        }
                        {
                        }
                        
                    """.trimIndent()
                )
            )
        }
    }


}