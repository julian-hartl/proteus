package lang.proteus.integration

import lang.proteus.api.ProteusCompiler
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertEquals

class ExampleTests {
    companion object {

        @JvmStatic
        fun getExampleTestFile(path: String): String {
            return System.getProperty("user.dir") +  "/src/test/resources/examples/$path"
        }

        @JvmStatic
        val exampleTestArguments: Stream<Arguments> = Stream.of(
            Arguments.of(
                "random_multiply/main.psl",
                3
            ),
            Arguments.of(
                "shadowing/main.psl",
                5
            )
        )
    }

    @ParameterizedTest
    @MethodSource("getExampleTestArguments")
    fun testExamples(mainFilePath: String, expectedValue: Any) {
        val path = getExampleTestFile(mainFilePath)
        val compiler = ProteusCompiler(
            outputGeneratedCode = false,
        )
        val result = compiler.interpretFile(path)
        val actualValue = result.evaluationResult?.value
        assertEquals(expectedValue, actualValue, "Expected value $expectedValue, but got $actualValue")
    }
}