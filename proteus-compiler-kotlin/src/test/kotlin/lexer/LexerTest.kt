package lexer

import kotlin.test.Test
import kotlin.test.assertEquals

class LexerTest {

    private lateinit var lexer: Lexer

    private fun initLexer(input: String) {
        lexer = Lexer(input)
    }

    @Test
    fun shouldParseBasicPlusOperation() {
        initLexer("1 + 2")
        assertEquals(SyntaxKind.NumberToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.WhiteSpaceToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.PlusToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.WhiteSpaceToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.NumberToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.EndOfFileToken, lexer.nextToken().kind)
    }

    @Test
    fun shouldParseOperationWithPlusAndMinus() {
        initLexer("1 + 2 - 3")
        assertEquals(SyntaxKind.NumberToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.WhiteSpaceToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.PlusToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.WhiteSpaceToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.NumberToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.WhiteSpaceToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.MinusToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.WhiteSpaceToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.NumberToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.EndOfFileToken, lexer.nextToken().kind)
    }

    @Test
    fun shouldParseOperationWithPlusAndMinusAndAsterisk() {
        initLexer("1 + 2 - 3 * 4")
        assertEquals(SyntaxKind.NumberToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.WhiteSpaceToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.PlusToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.WhiteSpaceToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.NumberToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.WhiteSpaceToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.MinusToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.WhiteSpaceToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.NumberToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.WhiteSpaceToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.AsteriskToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.WhiteSpaceToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.NumberToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.EndOfFileToken, lexer.nextToken().kind)
    }

    @Test
    fun shouldParseOperationWithPlusAndMinusAndAsteriskAndSlash() {
        initLexer("1 + 2 - 3 * 4 / 5")
        assertEquals(SyntaxKind.NumberToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.WhiteSpaceToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.PlusToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.WhiteSpaceToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.NumberToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.WhiteSpaceToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.MinusToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.WhiteSpaceToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.NumberToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.WhiteSpaceToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.AsteriskToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.WhiteSpaceToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.NumberToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.WhiteSpaceToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.SlashToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.WhiteSpaceToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.NumberToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.EndOfFileToken, lexer.nextToken().kind)
    }

    @Test
    fun shouldParseOperationWithParenthesis() {
        initLexer("(1 + 2) - (3 * 4) / 5")
        assertEquals(SyntaxKind.OpenParenthesisToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.NumberToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.WhiteSpaceToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.PlusToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.WhiteSpaceToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.NumberToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.CloseParenthesisToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.WhiteSpaceToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.MinusToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.WhiteSpaceToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.OpenParenthesisToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.NumberToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.WhiteSpaceToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.AsteriskToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.WhiteSpaceToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.NumberToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.CloseParenthesisToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.WhiteSpaceToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.SlashToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.WhiteSpaceToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.NumberToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.EndOfFileToken, lexer.nextToken().kind)
    }

    @Test
    fun shouldParseOperationWithBitwiseAnd() {
        initLexer("1 & 2")
        assertEquals(SyntaxKind.NumberToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.WhiteSpaceToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.AmpersandToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.WhiteSpaceToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.NumberToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.EndOfFileToken, lexer.nextToken().kind)
    }

    @Test
    fun shouldParseBadToken() {
        initLexer("1 © 2")
        assertEquals(SyntaxKind.NumberToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.WhiteSpaceToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.BadToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.WhiteSpaceToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.NumberToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.EndOfFileToken, lexer.nextToken().kind)
    }

    @Test
    fun shouldParseExpressionWithUnaryOperator() {
        initLexer("-1")
        assertEquals(SyntaxKind.MinusToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.NumberToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.EndOfFileToken, lexer.nextToken().kind)
    }

    @Test
    fun shouldParseExpressionWithParenthesisAndUnaryOperator() {
        initLexer("-(1 + 2)")
        assertEquals(SyntaxKind.MinusToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.OpenParenthesisToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.NumberToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.WhiteSpaceToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.PlusToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.WhiteSpaceToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.NumberToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.CloseParenthesisToken, lexer.nextToken().kind)
        assertEquals(SyntaxKind.EndOfFileToken, lexer.nextToken().kind)
    }
}