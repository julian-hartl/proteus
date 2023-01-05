package lang.proteus.syntax.parser

import kotlin.test.assertTrue

internal class ParserSyntaxChecker(private val input: String) {
    private fun checkSyntax(): Boolean {
        val parser = Parser(input)
        parser.parse()
        return !parser.hasErrors()
    }

    fun assertCorrectSyntax() {
        assertTrue(checkSyntax(), "Syntax should be correct for input: $input")
    }

    fun assertIncorrectSyntax() {
        assertTrue(!checkSyntax(), "Syntax should be incorrect for input: $input")
    }
}