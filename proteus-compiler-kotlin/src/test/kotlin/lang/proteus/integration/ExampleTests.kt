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
        val exampleTestArguments: Stream<Arguments> = Stream.of(
            Arguments.of(
                "src/test/resources/examples/random_multiply/main.psl",
                3
            )
        )
    }

    @ParameterizedTest
    @MethodSource("getExampleTestArguments")
    fun testExamples(mainFilePath: String, expectedValue: Any) {
        val compiler = ProteusCompiler()
        val result = compiler.compile(mainFilePath)
        val actualValue = result.evaluationResult?.value
        assertEquals(expectedValue, actualValue, "Expected value $expectedValue, but got $actualValue")
    }
}