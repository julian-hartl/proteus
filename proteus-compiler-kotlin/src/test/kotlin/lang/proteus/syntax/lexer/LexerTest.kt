package lang.proteus.syntax.lexer

import lang.proteus.syntax.parser.SyntaxTree
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals

class LexerTest {
    @ParameterizedTest(name = "Input `{1}` should lex to `{0}`")
    @MethodSource("getTokensWithWhiteSpaces")
    fun `lexer lexes token`(kind: Token, text: String) {
        val tokens = SyntaxTree.parseTokens(text)

        assertEquals(1, tokens.count())
        val token = tokens[0]
        assertEquals(kind, token.token)
        assertEquals(text, token.literal)
    }

    @ParameterizedTest(name = "Pair of tokens `{0}` and `{2}` should be lexed correctly.")
    @MethodSource("getTokenPairs")
    fun `lexer lexes token pairs`(kind1: Token, text1: String, kind2: Token, text2: String) {
        val tokens = SyntaxTree.parseTokens(text1 + text2)

        assertEquals(2, tokens.count())
        val token1 = tokens[0]
        assertEquals(kind1, token1.token)
        assertEquals(text1, token1.literal)
        val token2 = tokens[1]
        assertEquals(kind2, token2.token)
        assertEquals(text2, token2.literal)
    }

    @ParameterizedTest(name = "Pair of tokens `{0}` and `{4}` with separator {2} should be lexed correctly.")
    @MethodSource("getTokenPairsWithWhiteSpaces")
    fun `lexer lexes token pairs`(
        kind1: Token,
        text1: String,
        separator: Token,
        separatorText: String,
        kind2: Token,
        text2: String
    ) {
        val tokens = SyntaxTree.parseTokens(text1 + separatorText + text2)

        assertEquals(3, tokens.count())
        val token1 = tokens[0]
        assertEquals(kind1, token1.token)
        assertEquals(text1, token1.literal)
        val token2 = tokens[1]
        assertEquals(separator, token2.token)
        assertEquals(separatorText, token2.literal)
        val token3 = tokens[2]
        assertEquals(kind2, token3.token)
        assertEquals(text2, token3.literal)
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
                Arguments.of(Token.Identifier, "a"),
                Arguments.of(Token.Identifier, "abc"),

                Arguments.of(Keyword.True, "true"),
                Arguments.of(Keyword.False, "false"),

                Arguments.of(Operator.OpenParenthesis, "("),
                Arguments.of(Operator.CloseParenthesis, ")"),

                Arguments.of(Operator.Not, "not"),
                Arguments.of(Operator.And, "and"),
                Arguments.of(Operator.Or, "or"),
                Arguments.of(Operator.Xor, "xor"),

                Arguments.of(Operator.Ampersand, "&"),
                Arguments.of(Operator.Pipe, "|"),
                Arguments.of(Operator.Circumflex, "^"),

                Arguments.of(Operator.Equals, "="),

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
                Arguments.of(Operator.DoubleCircumflex, "^^"),

                Arguments.of(Operator.Is, "is"),
                Arguments.of(Operator.TypeOf, "typeof"),

                Arguments.of(Operator.DoubleLessThan, "<<"),
                Arguments.of(Operator.DoubleGreaterThan, ">>"),

                Arguments.of(Token.Number, "1"),
                Arguments.of(Token.Number, "200"),


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
            if (t1Token is Token.Identifier && t2Token is Token.Identifier) return true
            if (t1Token is Keyword && t2Token is Keyword) return true
            if (t1Token is Keyword && t2Token is Token.Identifier) return true
            if (t1Token is Token.Identifier && t2Token is Keyword) return true
            if (t1Token is Token.Number && t2Token is Token.Number) return true
            val isFirstIdentifierOrKeyword = t1Token is Token.Identifier || t1Token is Keyword
            val isSecondIdentifierOrKeyword = t2Token is Token.Identifier || t2Token is Keyword
            if (isFirstIdentifierOrKeyword) {
                return isWordOperator(t2Token)
            }
            if (isSecondIdentifierOrKeyword) {
                return isWordOperator(t1Token)
            }
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
            if (t1Token is Operator.Circumflex && t2Token is Operator.DoubleCircumflex) return true

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