package lang.proteus.text

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertEquals


class SourceTextTest {
    @ParameterizedTest
    @MethodSource
    fun `should return correct line position`(text: String, position: Int, expectedLine: Int) {
        val sourceText = SourceText.from(text)
        val actualLine = sourceText.getLineIndex(position)
        assertEquals(expectedLine, actualLine, "Expected line $expectedLine, but got $actualLine")
    }

    companion object {
        @JvmStatic
        fun `should return correct line position`(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("single line", 3, 0),
                Arguments.of("single line", 0, 0),
                Arguments.of("single line", 10, 0),
                Arguments.of(
                    """
                    first line
                    second line
                    third line
                """.trimIndent(), 0, 0
                ),
                Arguments.of(
                    """
                    first line
                    second line
                    third line
                """.trimIndent(), 10, 0
                ),
                Arguments.of(
                    """
                    first line
                    second line
                    third line
                """.trimIndent(), 11, 1
                ),
                Arguments.of(
                    """
                    first line
                    second line
                    third line
                """.trimIndent(), 12, 1
                ),
                Arguments.of(
                    """
                    first line
                    second line
                    third line
                """.trimIndent(), 13, 1
                ),
                Arguments.of(
                    """
                    first line
                    second line
                    third line
                """.trimIndent(), 21, 1
                ),
                Arguments.of(
                    """
                    first line
                    second line
                    third line
                """.trimIndent(), 22, 2
                ),
            )
        }
    }
}