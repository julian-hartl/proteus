package lang.proteus.syntax.lexer

import lang.proteus.syntax.lexer.token.Keyword
import lang.proteus.syntax.lexer.token.Operator
import lang.proteus.syntax.lexer.token.Token
import lang.proteus.syntax.lexer.token.Tokens
import lang.proteus.syntax.parser.SyntaxTree
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.Test
import kotlin.test.assertEquals

internal class LexerTest {

    @Test
    fun thereShouldNotBeAnyUntestedTokens() {
        val tokens = getTokensWithWhiteSpaces()

        val testedTokens = tokens.map {
            it.get()[0] as Token
        }

        val untestedTokens = Tokens.allTokens.filter { token ->
            testedTokens.contains(token).not()
        }.toMutableList()

        untestedTokens.removeAll(
            listOf(
                Token.Bad,
                Token.EndOfFile,
                Token.Expression,
                Token.Statement,
                Token.ElseClause
            )
        )



        assertEquals(0, untestedTokens.size, "There are untested tokens: $untestedTokens")

    }

    @Test
    fun `lexer lexes unterminated string`() {
        val input = "\"hello"
        val actual = SyntaxTree.parse(input)
        val diagnostics = actual.diagnostics.diagnostics
        assertEquals(1, diagnostics.size)
        assertEquals("Unterminated string literal", diagnostics[0].message)
    }

    @Test
    fun `should lex bad token`() {
        val input = "~"
        val actual = SyntaxTree.parseTokens(input)
        assertEquals(1, actual.size)
        val token = actual.first()
        assertEquals(Token.Bad, token.token)
        assertEquals("\u0000", token.literal)
    }

    @Test
    fun `should not be stuck in infinite loop`() {
        val input = """
            {
                var a = 0
                whilee a > 0 {
                    a = a - 1
                }
                a
            }
        """.trimIndent()
        SyntaxTree.parseTokens(input)
    }

    @Test
    fun `should lex identifier`() {
        val input = "abc"
        val actual = SyntaxTree.parseTokens(input)
        assertEquals(1, actual.size)
        val token = actual.first()
        assertEquals(Token.Identifier, token.token)
        assertEquals("abc", token.literal)
    }

    @ParameterizedTest(name = "Input `{1}` should lex to `{0}`")
    @MethodSource("getTokensWithWhiteSpaces")
    fun `lexer lexes token`(kind: Token, text: String) {
        val tokens = SyntaxTree.parseTokens(text)

        assertEquals(1, tokens.count())
        val token = tokens[0]
        assertEquals(kind, token.token)
        assertLiteral(text, token)
    }

    @ParameterizedTest(name = "Pair of tokens `{0}` [{1}] and `{2}` [{3}] should be lexed correctly.")
    @MethodSource("getTokenPairs")
    fun `lexer lexes token pairs`(kind1: Token, text1: String, kind2: Token, text2: String) {
        val tokens = SyntaxTree.parseTokens(text1 + text2)

        assertEquals(2, tokens.count())
        val token1 = tokens[0]
        assertEquals(kind1, token1.token)
        assertLiteral(text1, token1)
        val token2 = tokens[1]
        assertEquals(kind2, token2.token)
        assertLiteral(text2, token2)
    }

    @ParameterizedTest(name = "Pair of tokens `{0}` {1} and `{4}`{5} with separator {2} should be lexed correctly.")
    @MethodSource("getTokenPairsWithWhiteSpaces")
    fun `lexer lexes token pairs`(
        kind1: Token,
        text1: String,
        separator: Token,
        separatorText: String,
        kind2: Token,
        text2: String,
    ) {
        val tokens = SyntaxTree.parseTokens(text1 + separatorText + text2)

        assertEquals(3, tokens.count())
        val token1 = tokens[0]
        assertEquals(kind1, token1.token)
        assertLiteral(text1, token1)
        val token2 = tokens[1]
        assertEquals(separator, token2.token)
        assertLiteral(separatorText, token2)
        val token3 = tokens[2]
        assertEquals(kind2, token3.token)
        assertLiteral(text2, token3)
    }

    private fun assertLiteral(literal: String, token: SyntaxToken<*>) {
        if (token.token is Token.String) {
            assertEquals(literal.substring(1, literal.length - 1), token.literal)
        } else {
            assertEquals(literal, token.literal)
        }
    }

    companion object {

        @JvmStatic
        private fun getTokensWithWhiteSpaces(): List<Arguments> {
            return listOf(
                *getTokens().toTypedArray(),
                *getSeparators().toTypedArray(),
            )
        }

        @JvmStatic
        private fun getTokens(): List<Arguments> {
            return listOf(
                Arguments.of(Operator.Not, "not"),
                Arguments.of(Operator.And, "and"),
                Arguments.of(Operator.Or, "or"),
                Arguments.of(Operator.Xor, "xor"),

                Arguments.of(Token.Identifier, "a"),
                Arguments.of(Token.Identifier, "abc"),

                Arguments.of(Keyword.True, "true"),
                Arguments.of(Keyword.False, "false"),

                Arguments.of(Operator.OpenParenthesis, "("),
                Arguments.of(Operator.CloseParenthesis, ")"),


                Arguments.of(Operator.Ampersand, "&"),
                Arguments.of(Operator.Pipe, "|"),
                Arguments.of(Operator.Circumflex, "^"),

                Arguments.of(Operator.Equals, "="),
                Arguments.of(Operator.PlusEquals, "+="),
                Arguments.of(Operator.MinusEquals, "-="),

                Arguments.of(Operator.DoubleEquals, "=="),
                Arguments.of(Operator.NotEquals, "!="),
                Arguments.of(Operator.LessThan, "<"),
                Arguments.of(Operator.LessThanEquals, "<="),
                Arguments.of(Operator.GreaterThan, ">"),
                Arguments.of(Operator.GreaterThanEquals, ">="),

                Arguments.of(Operator.Plus, "+"),
                Arguments.of(Operator.Minus, "-"),
                Arguments.of(Operator.Asterisk, "*"),
                Arguments.of(Operator.Slash, "/"),
                Arguments.of(Operator.DoubleAsterisk, "**"),
                Arguments.of(Operator.Percent, "%"),

                Arguments.of(Operator.Is, "is"),
                Arguments.of(Operator.TypeOf, "typeof"),

                Arguments.of(Operator.DoubleLessThan, "<<"),
                Arguments.of(Operator.DoubleGreaterThan, ">>"),

                Arguments.of(Token.Number, "1"),
                Arguments.of(Token.Number, "200"),

                Arguments.of(Token.String, "\"test\""),

                Arguments.of(Token.Type, "Int"),

                Arguments.of(Token.SingleQuote, "'"),

                Arguments.of(Token.OpenBrace, "{"),
                Arguments.of(Token.CloseBrace, "}"),

                Arguments.of(Keyword.Val, "val"),
                Arguments.of(Keyword.Var, "var"),

                Arguments.of(Keyword.If, "if"),
                Arguments.of(Keyword.Else, "else"),

                Arguments.of(Keyword.While, "while"),
                Arguments.of(Keyword.For, "for"),

                Arguments.of(Keyword.Until, "until"),
                Arguments.of(Keyword.In, "in"),

                Arguments.of(Keyword.As, "as"),

                Arguments.of(Token.SemiColon, ";"),
                Arguments.of(Token.Comma, ","),

                )
        }

        private fun getSeparators(): List<Arguments> {
            return listOf(
                Arguments.of(Token.Whitespace, " "),
                Arguments.of(Token.Whitespace, "  "),
                Arguments.of(Token.Whitespace, "\r"),
                Arguments.of(Token.Whitespace, "\n"),
                Arguments.of(Token.Whitespace, "\r\n"),
            )
        }

        private fun requiresSeparator(t1Token: Token, t2Token: Token): Boolean {
            if (t1Token is Token.String && t2Token is Token.String) return true
            if (t1Token is Token.Identifier && t2Token is Token.Identifier) return true
            if (t1Token is Keyword && t2Token is Keyword) return true
            if (t1Token is Keyword && t2Token is Token.Identifier) return true
            if (t1Token is Token.Identifier && t2Token is Keyword) return true
            if (t1Token is Token.Number && t2Token is Token.Number) return true
            if (t1Token is Token.Type || t2Token is Token.Type) return true
            if (t1Token is Operator.Asterisk) {
                return t2Token is Operator.Asterisk || t2Token is Operator.DoubleAsterisk
            }
            val isFirstIdentifierOrKeyword = t1Token is Token.Identifier || t1Token is Keyword
            val isSecondIdentifierOrKeyword = t2Token is Token.Identifier || t2Token is Keyword

            if (isFirstIdentifierOrKeyword) {
                return isWordOperator(t2Token)
            }
            if (isSecondIdentifierOrKeyword) {
                return isWordOperator(t1Token)
            }
            if (isWordOperator(t1Token) && isWordOperator(t2Token)) return true
            if (t1Token is Operator.Equals && t2Token is Operator.Equals) return true
            if (t1Token is Operator.Equals && t2Token is Operator.DoubleEquals) return true
            if (t1Token is Operator.LessThan && t2Token is Operator.LessThan) return true
            if (t1Token is Operator.GreaterThan && t2Token is Operator.GreaterThan) return true
            if (t1Token is Operator.LessThan && t2Token is Operator.Equals) return true
            if (t1Token is Operator.LessThan && t2Token is Operator.DoubleEquals) return true
            if (t1Token is Operator.GreaterThan && t2Token is Operator.Equals) return true
            if (t1Token is Operator.GreaterThan && t2Token is Operator.DoubleEquals) return true
            if (t1Token is Operator.LessThan && t2Token is Operator.DoubleLessThan) return true
            if (t1Token is Operator.GreaterThan && t2Token is Operator.DoubleGreaterThan) return true
            if (t1Token is Operator.DoubleLessThan && t2Token is Operator.LessThan) return true
            if (t1Token is Operator.DoubleGreaterThan && t2Token is Operator.GreaterThan) return true
            if (t1Token is Operator.GreaterThan && t2Token is Operator.GreaterThanEquals) return true
            if (t1Token is Operator.LessThan && t2Token is Operator.LessThanEquals) return true
            if (t1Token is Operator.Circumflex && t2Token is Operator.Circumflex) return true
            if (t1Token is Operator.Circumflex && t2Token is Operator.DoubleAsterisk) return true
            if (t1Token is Operator.Plus && t2Token is Operator.Equals) return true
            if (t1Token is Operator.Minus && t2Token is Operator.Equals) return true
            if (t1Token is Operator.Plus && t2Token is Operator.DoubleEquals) return true
            if (t1Token is Operator.Minus && t2Token is Operator.DoubleEquals) return true

            return false
        }

        private fun isWordOperator(token: Token): Boolean {
            return token is Operator.And || token is Operator.Or || token is Operator.Xor || token is Operator.Not || token is Operator.Is || token is Operator.TypeOf
        }

        @JvmStatic
        private fun getTokenPairs(): List<Arguments> {
            val args = mutableListOf<Arguments>()
            for (arg1 in getTokens()) {
                val token1 = arg1.get()[0] as Token
                val text1 = arg1.get()[1] as String
                for (arg2 in getTokens()) {
                    val token2 = arg2.get()[0] as Token
                    val text2 = arg2.get()[1] as String

                    if (!requiresSeparator(token1, token2)) {
                        args.add(Arguments.of(token1, text1, token2, text2))
                    }
                }
            }
            return args
        }

        @JvmStatic
        private fun getTokenPairsWithWhiteSpaces(): List<Arguments> {
            val args = mutableListOf<Arguments>()
            for (arg1 in getTokens()) {
                val token1 = arg1.get()[0] as Token
                val text1 = arg1.get()[1] as String
                for (arg2 in getTokens()) {
                    val token2 = arg2.get()[0] as Token
                    val text2 = arg2.get()[1] as String

                    if (requiresSeparator(token1, token2)) {
                        for (arg3 in getSeparators()) {
                            val separator = arg3.get()[0] as Token
                            val separatorText = arg3.get()[1] as String
                            args.add(Arguments.of(token1, text1, separator, separatorText, token2, text2))
                        }
                    }
                }
            }
            return args
        }
    }
}