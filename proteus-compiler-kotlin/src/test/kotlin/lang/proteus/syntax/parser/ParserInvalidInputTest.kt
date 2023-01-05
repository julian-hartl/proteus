package lang.proteus.syntax.parser

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class ParserInvalidInputTest {

    @ParameterizedTest
    @MethodSource("getInvalidSyntaxInputs")
    fun `invalid syntax tests`(input: String) {
        val parserSyntaxChecker = ParserSyntaxChecker(input)
        parserSyntaxChecker.assertIncorrectSyntax()
    }


    companion object {
        @JvmStatic
        fun getInvalidSyntaxInputs(): List<Arguments> {
            val inputs = listOf(
                "2 ( 2",
                "2 ) 2",
                "2 ( 2 )",
                "2 ) ( 2",
                "2 ( 2 ) ( 2",
                "2 ( 2 ) ) 2",
                "2 2",
                "2 2 2",
                "1 -",
                "1 +",
                "1 *",
                "1 /",
                "1 ^^",
            )
            return inputs.map {
                Arguments.of(it)
            }
        }
    }
}