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
            program
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
                            let a = 1;
                            let b = 2;
                        }
                    """.trimIndent(),
                    """
                        fn main_0() -> Unit {
                            let a_main_0: Int = 1;
                            let b_main_0: Int = 2;
                            return;
                        }
                       
                    """.trimIndent()
                ),
                Arguments.of(
                    """
                        fn main() {
                            let a = 1;
                            let b = 2;
                            let c = a + b;
                        }
                    """.trimIndent(),
                    """
                        fn main_0() -> Unit {
                            let a_main_0: Int = 1;
                            let b_main_0: Int = 2;
                            let c_main_0: Int = ((a_main_0) + (b_main_0));
                            return;
                        }
                   
                    """.trimIndent()
                ),
                Arguments.of(
                    """
                        fn main() {
                            let test = "test";
                        }
                    """.trimIndent(),
                    """
                        fn main_0() -> Unit {
                            let test_main_0: String = "test";
                            return;
                        }
                     
                    """.trimIndent()
                ),
                Arguments.of(
                    """
                        fn main() {
                        if (true) {
                            let a = 1;
                            let b = 2;
                            let c = a + b;
                            }
                            else {
                            let a = 1;
                            }
                        }
                    """.trimIndent(),
                    """
                        fn main_0() -> Unit {
                            gotoIfFalse else_0 true;
                            let a_main_0: Int = 1;
                            let b_main_0: Int = 2;
                            let c_main_0: Int = ((a_main_0) + (b_main_0));
                            goto if_0_end;
                            else_0:
                            let a_main_0: Int = 1;
                            if_0_end:
                            return;
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
                        fn main_0() -> Unit {
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
                        fn main_0() -> Int {
                            return ((1) + (1));
                        }
                        
                    """.trimIndent()
                )
            )
        }
    }


}