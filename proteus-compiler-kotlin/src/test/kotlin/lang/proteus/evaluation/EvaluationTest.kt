package lang.proteus.evaluation

import lang.proteus.api.Compilation
import lang.proteus.api.ProteusCompiler
import lang.proteus.binding.*
import lang.proteus.symbols.TypeSymbol
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

        ProteusCompiler().interpretText(text)
    }

    @ParameterizedTest
    @MethodSource("getValidSyntaxInputs")
    fun `test valid inputs to evaluate correctly`(input: String, value: Any) {
        val tree = SyntaxTree.parse(
            """
            fn main() {
                $input
            }
        """.trimIndent()
        )
        val compilation = Compilation.interpret(tree)

        val evaluationResult = compilation.evaluate(mutableMapOf()) {

        }
        assertFalse(
            evaluationResult.diagnostics.hasErrors(),
            "Compilation should not have errors, but it has: ${evaluationResult.diagnostics}"
        )
        assertEquals(
            value,
            evaluationResult.value,
            "Evaluation result should be $value, but was ${evaluationResult.value}"
        )
    }

    @ParameterizedTest
    @MethodSource("validSyntaxWithoutMainFunction")
    fun `test valid inputs to evaluate correctly without main function`(input: String, value: Any) {
        val expression = SyntaxTree.parse(
            input
        )
        val compilation = Compilation.interpret(expression)

        val evaluationResult = compilation.evaluate(mutableMapOf()) {

        }
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
        assertValue(
            """
            fn main(){
                $input
            }
        """.trimIndent(), expectedValue
        )
    }

    private fun assertValue(input: String, expectedValue: Any) {
        val compiler = ProteusCompiler(
            outputGeneratedCode = false,
        )
        val result = compiler.interpretText(input)
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
                
                    1;
                    2;
                    3;
                
            """.trimIndent(), 3
            ),
            Arguments.of(
                """
                
                    1;
                    5 * 5;
                
            """.trimIndent(), 25
            ),
        )

        @JvmStatic
        fun getValidSyntaxInputs(): List<Arguments> {
            return listOf(
                Arguments.of("2;", 2),
                Arguments.of("(2);", 2),
                Arguments.of("+2;", 2),
                Arguments.of("-2;", -2),
                Arguments.of("2 + 2;", 4),
                Arguments.of("2 - 2;", 0),
                Arguments.of("2 * 2;", 4),
                Arguments.of("2 / 2;", 1),
                Arguments.of("2 ** 3;", 8),
                Arguments.of("2 + 2 * 2;", 6),
                Arguments.of("2 * (2 + 2);", 8),
                Arguments.of("2 * (2 + 2) / 2;", 4),
                Arguments.of("7 % 2;", 1),

                Arguments.of("1 & 2;", 0),
                Arguments.of("2 | 2;", 2),
                Arguments.of("1 | 2;", 3),
                Arguments.of("2 ^ 2;", 0),
                Arguments.of("2 & 2 | 2;", 2),
                Arguments.of("2 << 2;", 8),
                Arguments.of("8 >> 2;", 2),

                Arguments.of("true;", true),
                Arguments.of("false;", false),
                Arguments.of("true and true;", true),
                Arguments.of("true and false;", false),
                Arguments.of("false and true;", false),
                Arguments.of("false and false;", false),
                Arguments.of("true or true;", true),
                Arguments.of("true or false;", true),
                Arguments.of("false or true;", true),
                Arguments.of("false or false;", false),
                Arguments.of("not true;", false),
                Arguments.of("not false;", true),
                Arguments.of("true and true or false;", true),
                Arguments.of("true and (true or false);", true),
                Arguments.of("true and (true or false) and true;", true),
                Arguments.of("true and (true or false) and false;", false),
                Arguments.of("true and (true or false) and not false;", true),
                Arguments.of("true and (true or false) and not false or false;", true),
                Arguments.of("true xor true;", false),
                Arguments.of("true xor false;", true),
                Arguments.of("false xor true;", true),
                Arguments.of("false xor false;", false),
                Arguments.of("(true) xor (true);", false),
                Arguments.of("(true) xor (false);", true),
                Arguments.of("false == false;", true),
                Arguments.of("false == true;", false),
                Arguments.of("true == false;", false),
                Arguments.of("true == true;", true),
                Arguments.of("false != false;", false),
                Arguments.of("false != true;", true),
                Arguments.of("true != false;", true),
                Arguments.of("true != true;", false),
                Arguments.of("true == true and false == false;", true),
                Arguments.of("true == true and false == false or false == true;", true),
                Arguments.of("1 == 1;", true),
                Arguments.of("1 == 2;", false),
                Arguments.of("1 != 1;", false),
                Arguments.of("1 != 2;", true),

                Arguments.of("1 < 2;", true),
                Arguments.of("1 < 1;", false),
                Arguments.of("2 < 1;", false),
                Arguments.of("1 <= 1;", true),
                Arguments.of("1 <= 1;", true),
                Arguments.of("2 <= 1;", false),
                Arguments.of("1 > 2;", false),
                Arguments.of("1 > 1;", false),
                Arguments.of("2 > 1;", true),
                Arguments.of("1 >= 2;", false),
                Arguments.of("1 >= 1;", true),
                Arguments.of("2 + 1 >= 1;", true),
                Arguments.of("2 >= 1;", true),
                Arguments.of("1 >= 2;", false),
                Arguments.of("1 >= 1;", true),
                Arguments.of("2 + 1 >= 1;", true),
                Arguments.of("( 2 * 2 ) > 3;", true),

                Arguments.of("typeof 2;", TypeSymbol.Int),
                Arguments.of("typeof true;", TypeSymbol.Boolean),
                Arguments.of("typeof \"test\";", TypeSymbol.String),
                Arguments.of("typeof false == Boolean;", true),
                Arguments.of("typeof false == Int;", false),


                Arguments.of("1 is Int;", true),
                Arguments.of("1 is Boolean;", false),
                Arguments.of("1 is Int and 1 is Boolean;", false),
                Arguments.of("1 is Int or 1 is Boolean;", true),

                Arguments.of(" let mut a = 0; a = (a = 10) * 10; ", 100),
                Arguments.of(" let a = 42; a;", 42),
                Arguments.of(" let b = -4; b;", -4),
                Arguments.of(
                    """
                    
                        let a = 42;
                        let b = a + 1;
                        b;
                    
                """.trimIndent(), 43
                ),

                Arguments.of(
                    """
                    
                        let mut a = 0;
                        a += 10;
                        a;
                    
                """.trimIndent(), 10
                ),

                Arguments.of(
                    """
                    
                        let mut a = 0;
                        a -= 10;
                        a;
                    
                """.trimIndent(), -10
                ),

                Arguments.of(
                    """
                        
                            let mut x = 1 + 2;
                            typeof x;
                        

                    """.trimIndent(),
                    TypeSymbol.Int
                ),

                Arguments.of(
                    """
                    
                       let mut x = 10;
                       if x == 10
                            x = 20;
                       x;
                    
                """.trimIndent(), 20
                ),

                Arguments.of(
                    """
                    
                        let mut x = 10;
                        if x == 20 {
                            x = 5;
                        }
                        else {
                            x = 7;
                        }
                        x;
                    
                """.trimIndent(), 7
                ),

                Arguments.of(
                    """
                    
                        let mut x = 10;
                        while x > 0
                            x = x - 1;
                        x;
                    
                """.trimIndent(), 0
                ),

                Arguments.of(
                    """
                            let mut b = 0;
                            for x in 1 until 10 {
                                b = b + x;
                            }
                            b;
                """.trimIndent(), 55
                ),

                Arguments.of(
                    """
                        
                            let mut b = 0;
                            if b == 0
                                b = 10;
                            b;
                        
                    """.trimIndent(), 10
                ),

                Arguments.of(
                    """
                        
                            let mut b = 10;
                            for x in 1 until 10 {
                                b += x;
                            }
                            b;
                        
                    """.trimIndent(), 65
                ),

                Arguments.of(
                    """
                       
                            let mut b = 0;
                            if (b == 0) {
                                b = 10;
                            };
                            b;
                        
                    """.trimIndent(),
                    10
                ),

                Arguments.of(
                    """

                            let test = "{ val test = a; }";
                            let mut a = 0;
                            if test == "{ val test = a; }" {
                                a = 10;
                            } else {
                                a = 2;
                            }
                            a;
                        
                    """.trimIndent(),
                    10
                ),

                Arguments.of(
                    """
                    {
                    let a = "Hello" + " " + "World";
                    a;
                    }
                """.trimIndent(),
                    "Hello World"
                ),



                Arguments.of(
                    """
                        "test" == "test";
                    """.trimIndent(), true
                ),
                Arguments.of(
                    """
                        "test" == "tewest";
                    """.trimIndent(), false
                ),
                Arguments.of(
                    """
                        "test" != "test";
                    """.trimIndent(), false
                ),
                Arguments.of(
                    """
                        "test" != "teest";
                    """.trimIndent(), true
                ),
                Arguments.of(
                    """
                        "test" + "test";
                    """.trimIndent(), "testtest"
                ),
                Arguments.of(
                    """
                        let hello = "Hello";
                        let name = "World";
                        hello + " " + name;
                    """.trimIndent(), "Hello World"
                ),
                Arguments.of(
                    """
                        let hello = "Hello";
                        let number = 2;
                        hello + " " + number as String;
                    """.trimIndent(), "Hello 2"
                ),

                Arguments.of(
                    """
                    let mut a = 1;
                    while true {
                        if a == 10 {
                            break;
                        }
                        a += 1;
                    }
                """.trimIndent(), 10
                ),

                Arguments.of(
                    """
                    let mut a = 0;
                    let mut b = 0;
                    while true {
                        if a == 10 break;
                        a += 1;
                        if a == 2 {
                           continue;
                        }
                        b += a;
                    }
                """.trimIndent(), 53
                ),

                Arguments.of(
                    """
                        let mut i = 0;
                        while i < 5 {
                            i += 1;
                            if i == 5 continue;
                        }
                        i;
                    """.trimIndent(), 5
                ),


                )
        }

        @JvmStatic
        fun validSyntaxWithoutMainFunction(): Stream<Arguments> = Stream.of(
            Arguments.of(
                """
                        fn test() -> Int{
                            return 10;
                        }
                        
                        fn main() {
                            let x = test();
                        }
                    """.trimIndent(),
                10,
            ),

            Arguments.of(
                """
                    import "std/math";
                        fn main() {
                            let random = "1";
                            random(random as Int, 1);
                        }
                    """.trimIndent(), 1
            ),

            Arguments.of(
                """
                        let mut a = 0;
                        fn test() {
                            if a == 0 {
                                return;
                            }
                            else {
                                a = 10;
                            }
                            
                        }
                        fn main() {
                            test();
                            let x = a;
                        }
                    """.trimIndent(),
                0,
            ),

            Arguments.of(
                """
                    fn sum(n: Int) -> Int {
                        let mut i = n;
                        let mut result = 0;
                        while true {
                            if i == 0 return result;
                            result += i;
                            i -= 1;
                        }
                    }

                    fn main() {
                        let sum = sum(90);
                    }
                """.trimIndent(), 4095
            ),

            Arguments.of(
                """
                    struct Test {
                        a: Int;
                        b: Int;
                    }
                    
                    fn main() {
                        let test = Test {
                            a: 10,
                            b: 20,
                        };
                        let a = test.a;
                        let b = test.b;
                    }
                """.trimIndent(),
                20
            ),

            Arguments.of(
                """
                    struct Test {
                        a: Int;
                        mut b: Int;
                    }
                    
                    fn main() {
                        let test = Test {
                            a: 10,
                            b: 20,
                        };
                        test.b = 30;
                        let a = test.a;
                        let b = test.b;
                    }
                """.trimIndent(),
                30
            ),

            Arguments.of(
                """
                    fn main() {
                        let test = &mut 10;
                        
                        let a = *test;
                    }
                """.trimIndent(),
                10
            )
        )

    }

    @Test
    fun `evaluator let mutiable declaration reports redeclaration`() {
        val text = """
            
                let mut x = 10;
                let mut y = 100;
                {
                    let mut x = 10;
                }
                let mut [x] = 5;
            
        """

        val diagnostics = "Variable 'x' already declared"
        assertDiagnostics(text, diagnostics)
    }

    @Test
    fun `evaluator NameExpression reports undefined`() {
        val text = """

            [x] * 10;
        """

        val diagnostics = "Unresolved reference: x"
        assertDiagnostics(text, diagnostics)
    }

    @Test
    fun `evaluator Assignment reports unresolved reference`() {
        val text = """
            [x] = 10;
        """

        val diagnostics = "Unresolved reference: x"
        assertDiagnostics(text, diagnostics)
    }

    @Test
    fun `evaluator Assignment reports cannot be reassigned`() {
        val text = """
            
                let x = 10;
                [x] = 20;
            
        """
        val diagnostics = "Immutable variable 'x' cannot be reassigned --- Hint: Use 'let mut x = ...' to declare a mutable variable"
        assertDiagnostics(text, diagnostics)
    }

    @Test
    fun `evaluator Assignment reports cannot convert`() {
        val text = """

                let mut x = 10;
                x = [false];
            
        """
        val diagnostics = "Cannot convert type 'Boolean' to 'Int'"
        assertDiagnostics(text, diagnostics)
    }

    @Test
    fun `evaluator Binary reports undefined operator`() {
        val text = """
            
                let mut x = 5 [+] false;
            
        """
        val diagnostics = "Operator '+' is not defined for types 'Int' and 'Boolean'"
        assertDiagnostics(text, diagnostics)
    }

    @Test
    fun `evaluator Unary reports undefined operator`() {
        val text = """
            
                let mut x = [+]false;

        """
        val diagnostics = "Operator '+' is not defined for type 'Boolean'"
        assertDiagnostics(text, diagnostics)
    }

    @Test
    fun `evaluator If reports not Boolean condition`() {
        val text = """
            
                if [10] {
                    let mut x = 10;
                }
            
        """
        val diagnostics = "Cannot convert type 'Int' to 'Boolean'"
        assertDiagnostics(text, diagnostics)
    }

    @Test
    fun `evaluator While reports not Boolean condition`() {
        val text = """
            
                while [10] {
                    let mut x = 10;
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
            []
        """
        val diagnostics = """
           Expected a global statement
        """.trimIndent()
        assertDiagnostics(text, diagnostics, wrapInMain = false)
    }

    @Test
    fun `evaluator final let mutable should not be reassigned with += operator`() {
        val text = """
            
                let a = 2;
                [a] += 1;
            
        """.trimIndent()
        val diagnostics = "Immutable variable 'a' cannot be reassigned --- Hint: Use 'let mut a = ...' to declare a mutable variable"

        assertDiagnostics(text, diagnostics)
    }

    @Test
    fun `evaluator final let mutable should not be reassigned with -= operator`() {
        val text = """
            
                let a = 2;
                [a] -= 1;
            
        """.trimIndent()
        val diagnostics = "Immutable variable 'a' cannot be reassigned --- Hint: Use 'let mut a = ...' to declare a mutable variable"

        assertDiagnostics(text, diagnostics)
    }


    @Test
    fun `evaluator += operator should not be applicable to Boolean`() {
        val text = """
            
                let mut a = true;
                a += [1];
            
        """.trimIndent()
        val diagnostics = "Cannot convert type 'Int' to 'Boolean'"

        assertDiagnostics(text, diagnostics)
    }

    @Test
    fun `evaluator -= operator should not be applicable to Boolean`() {
        val text = """
            
                let mut a = true;
                a -= [1];
            
        """.trimIndent()
        val diagnostics = "Cannot convert type 'Int' to 'Boolean'"

        assertDiagnostics(text, diagnostics)
    }

    @Test
    fun `should not be able to use while statement as top level statement`() {
        val text = """
            [while true {
                let mut a = 1;
            }]
            
            fn main() {
            }
        """.trimIndent()
        val diagnostics = """
            Invalid top-level statement
        """.trimIndent()
        assertDiagnostics(text, diagnostics, wrapInMain = false)
    }

    @Test
    fun `should not be able to reassign let mutable not marked as mut`() {
        val text = """
            fn main() {
                let a = 1;
                [a] = 2;
            }
        """.trimIndent()
        val diagnostics = """
            Immutable variable 'a' cannot be reassigned --- Hint: Use 'let mut a = ...' to declare a mutable variable
        """.trimIndent()
        assertDiagnostics(text, diagnostics, wrapInMain = false)
    }

    @Test
    fun `should not be able to reassign not mutable struct member`() {
        val text = """
            struct Foo {
                a: Int;
            }
            
            fn main() {
                let foo = Foo { a: 1 };
                [foo.a] = 2;
            }
        """.trimIndent()
        val diagnostics = """
            Member 'a' of struct 'Foo' is not mutable --- Hint: Use 'mut' to make it mutable
        """.trimIndent()
        assertDiagnostics(text, diagnostics, wrapInMain = false)
    }

    @Test
    fun `should not be able to reassign not mutable function parameter`() {
        val text = """
            fn foo(a: Int) {
                [a] = 2;
            }
            
            fn main() {
                foo(1);
            }
        """.trimIndent()
        val diagnostics = """
            Immutable parameter 'a' cannot be reassigned --- Hint: Use 'mut a: Int' to declare a mutable parameter
        """.trimIndent()
        assertDiagnostics(text, diagnostics, wrapInMain = false)
    }

    @Test
    fun `should not be able to reassign not mutable pointer`() {
        val text = """
            fn main() {
                let a = 1;
                let b = &a;
                [*b] = 2;
            }
        """.trimIndent()
        val diagnostics = """
            Cannot assign to immutable pointer '&Int' --- Hint: Use '&mut' to get a mutable pointer to the value.
        """.trimIndent()
        assertDiagnostics(text, diagnostics, wrapInMain = false)
    }

    @Test
    fun `should not be able to obtain mutable reference to immutable value`() {
        val text = """
            fn main() {
                let a = 1;
                let b = &[mut] a;
            }
        """.trimIndent()
        val diagnostics = """
            Cannot get mutable reference to 'Int'
        """.trimIndent()
        assertDiagnostics(text, diagnostics, wrapInMain = false)
    }

    @Test
    fun `should not be able to assign by reference to immutable struct member`() {
        val text = """
            struct Foo {
                a: Int;
            }
            
            fn main() {
                let foo = Foo { a: 1 };
                let b = &foo.a;
                [*b] = 2;
            }
        """.trimIndent()
        val diagnostics = """
            Cannot assign to immutable pointer '&Int' --- Hint: Use '&mut' to get a mutable pointer to the value.
        """.trimIndent()
        assertDiagnostics(text, diagnostics, wrapInMain = false)
    }

    @Test
    fun `should not be able to pass immutable pointer to function that requires mutable pointer`() {
        val text = """
            fn foo(a: &mut Int) {
            }
            
            fn main() {
                let a = 1;
                let b = &a;
                foo([b]);
            }
        """.trimIndent()
        val diagnostics = """
            Cannot convert type '&Int' to '&mut Int' --- Hint: You need to pass a mutable reference instead of an immutable one. Use &mut to get a mutable reference to the value.
        """.trimIndent()
        assertDiagnostics(text, diagnostics, wrapInMain = false)
    }

    @Test
    fun `should not be able to pass immutable reference to function that requires mutable reference`() {
        val text = """
            fn foo(a: &mut Int) {
            }
            
            fn main() {
                let a = 1;
                foo([&a]);
            }
        """.trimIndent()
        val diagnostics = """
            Cannot convert type '&Int' to '&mut Int' --- Hint: You need to pass a mutable reference instead of an immutable one. Use &mut to get a mutable reference to the value.
        """.trimIndent()
        assertDiagnostics(text, diagnostics, wrapInMain = false)
    }

    @Test
    fun `should not allow mutable constant`() {
        val text = """
            const [mut] a = 1;
        """.trimIndent()
        val diagnostics = """
            Cannot use 'mut' with 'const'
        """.trimIndent()
        assertDiagnostics(text, diagnostics, wrapInMain = false)
    }

    private fun assertDiagnostics(text: String, diagnosticText: String, wrapInMain: Boolean = true) {
        val wrappedText = if (wrapInMain) """
            fn main(){
                $text
            }
        """.trimIndent() else text
        val annotatedText = AnnotatedText.parse(wrappedText)
        val syntaxTree = SyntaxTree.parse(annotatedText.text)
        val compilation = Compilation.interpret(syntaxTree)
        val result = compilation.evaluate(mutableMapOf()) {

        }

        val expectedDiagnostics = AnnotatedText.unindentLines(diagnosticText)

        if (annotatedText.spans.size != expectedDiagnostics.size) {
            throw IllegalArgumentException("Number of diagnostics does not match number of spans.")
        }

        assertEquals(
            expectedDiagnostics.size,
            result.diagnostics.errors.size,
            "Did not get the expected number of diagnostics, actual: ${result.diagnostics.errors}."
        )

        var assertedDiagnostics = 0
        for (i in expectedDiagnostics.indices) {
            val expectedMessage = expectedDiagnostics[assertedDiagnostics]
            val actualMessage = result.diagnostics.errors[i].message
            assertEquals(expectedMessage, actualMessage, "Diagnostic message does not match.")

            val expectedSpan = annotatedText.spans[i]
            val actualSpan = result.diagnostics.errors[i].span
            assertEquals(expectedSpan, actualSpan, "Diagnostic span does not match: ${result.diagnostics.errors}")

            assertedDiagnostics++
        }
        assertEquals(
            expectedDiagnostics.size, assertedDiagnostics, """
            Did not get the expected amount diagnostics.
            Expected: $expectedDiagnostics
            Actual: ${result.diagnostics.errors}
        """.trimIndent()
        )
    }
}