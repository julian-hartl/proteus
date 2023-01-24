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
        SyntaxTree.reset()
        val parsed = SyntaxTree.parse(input)
        val globalScope = Binder.bindGlobalScope(null, parsed.root)
        val program = Binder.bindProgram(globalScope, parsed, optimize = false)
        return CodeGenerator.generate(
            program.functionBodies,
            program.globalScope.functions
        )
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
                    """.trimIndent(),
                    """
                        fn main@0() -> Unit {
                            var a@0: Int = 1;
                            var b@0: Int = 2;
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
                    """.trimIndent(),
                    """
                        fn main@0() -> Unit {
                            var a@0: Int = 1;
                            var b@0: Int = 2;
                            var c@0: Int = ((a@0) + (b@0));
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
                        fn main@0() -> Unit {
                            var test@0: String = "test";
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
                        fn main@0() -> Unit {
                            gotoIfFalse else_0 true;
                            var a@0: Int = 1;
                            var b@0: Int = 2;
                            var c@0: Int = ((a@0) + (b@0));
                            goto if_0_end;
                            else_0:
                            var a@0: Int = 1;
                            if_0_end:
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
                        fn main@0() -> Unit {
                            return;
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
                        fn main@0() -> Int {
                            return ((1) + (1));
                        }
                        
                    """.trimIndent()
                )
            )
        }
    }


}