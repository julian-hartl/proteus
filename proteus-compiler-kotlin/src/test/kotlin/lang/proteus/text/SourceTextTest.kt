package lang.proteus.text

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertEquals


class SourceTextTest {
    @ParameterizedTest
    @MethodSource
    fun `should return correct line position`(
        text: kotlin.String,
        position: Int,
        expectedLine: Int,
        amountOfLines: Int,
    ) {
        val sourceText = SourceText.from(text)
        val actualLine = sourceText.getLineIndex(position)
        assertEquals(expectedLine, actualLine, "Expected line $expectedLine, but got $actualLine")
        assertEquals(
            amountOfLines,
            sourceText.lines.size,
            "Expected $amountOfLines lines, but got ${sourceText.lines.size}"
        )
    }

    companion object {
        @JvmStatic
        fun `should return correct line position`(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("single line", 3, 0, 1),
                Arguments.of("single line", 0, 0, 1),
                Arguments.of("single line", 10, 0, 1),
                Arguments.of("first\nsecond", 6, 1, 2),
                Arguments.of("first\r\nsecond", 7, 1, 2),
                Arguments.of("first\rsecond", 6, 1, 2),
                Arguments.of(".\r\n\r\n", 0, 0, 3),
                Arguments.of(
                    """
                    first line
                    second line
                    third line
                """.trimIndent(), 0, 0, 3
                ),
                Arguments.of(
                    """
                    first line
                    second line
                    third line
                """.trimIndent(), 10, 0, 3
                ),
                Arguments.of(
                    """
                    first line
                    second line
                    third line
                """.trimIndent(), 11, 1, 3
                ),
                Arguments.of(
                    """
                    first line
                    second line
                    third line
                """.trimIndent(), 12, 1, 3
                ),
                Arguments.of(
                    """
                    first line
                    second line
                    third line
                """.trimIndent(), 13, 1, 3
                ),
                Arguments.of(
                    """
                    first line
                    second line
                    third line
                """.trimIndent(), 21, 1, 3
                ),
                Arguments.of(
                    """
                    first line
                    second line
                    third line
                """.trimIndent(), 23, 2, 3
                ),
            )
        }
    }
}