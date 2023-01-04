package lexer

import de.proteus.syntax.lexer.Lexer
import de.proteus.syntax.lexer.Operator
import kotlin.test.Test
import kotlin.test.assertEquals
import de.proteus.syntax.lexer.Token
class LexerTest {

    private lateinit var lexer: Lexer

    private fun initLexer(input: String) {
        lexer = Lexer(input)
    }

    @Test
    fun shouldParseBasicPlusOperation() {
        initLexer("1 + 2")
        assertEquals(Token.Number, lexer.nextToken().token)
        assertEquals(Token.Whitespace, lexer.nextToken().token)
        assertEquals(Operator.Plus, lexer.nextToken().token)
        assertEquals(Token.Whitespace, lexer.nextToken().token)
        assertEquals(Token.Number, lexer.nextToken().token)
        assertEquals(Token.EndOfFile, lexer.nextToken().token)
    }

    @Test
    fun shouldParseOperationWithPlusAndMinus() {
        initLexer("1 + 2 - 3")
        assertEquals(Token.Number, lexer.nextToken().token)
        assertEquals(Token.Whitespace, lexer.nextToken().token)
        assertEquals(Operator.Plus, lexer.nextToken().token)
        assertEquals(Token.Whitespace, lexer.nextToken().token)
        assertEquals(Token.Number, lexer.nextToken().token)
        assertEquals(Token.Whitespace, lexer.nextToken().token)
        assertEquals(Operator.Minus, lexer.nextToken().token)
        assertEquals(Token.Whitespace, lexer.nextToken().token)
        assertEquals(Token.Number, lexer.nextToken().token)
        assertEquals(Token.EndOfFile, lexer.nextToken().token)
    }

    @Test
    fun shouldParseOperationWithPlusAndMinusAndAsterisk() {
        initLexer("1 + 2 - 3 * 4")
        assertEquals(Token.Number, lexer.nextToken().token)
        assertEquals(Token.Whitespace, lexer.nextToken().token)
        assertEquals(Operator.Plus, lexer.nextToken().token)
        assertEquals(Token.Whitespace, lexer.nextToken().token)
        assertEquals(Token.Number, lexer.nextToken().token)
        assertEquals(Token.Whitespace, lexer.nextToken().token)
        assertEquals(Operator.Minus, lexer.nextToken().token)
        assertEquals(Token.Whitespace, lexer.nextToken().token)
        assertEquals(Token.Number, lexer.nextToken().token)
        assertEquals(Token.Whitespace, lexer.nextToken().token)
        assertEquals(Operator.Asterisk, lexer.nextToken().token)
        assertEquals(Token.Whitespace, lexer.nextToken().token)
        assertEquals(Token.Number, lexer.nextToken().token)
        assertEquals(Token.EndOfFile, lexer.nextToken().token)
    }

    @Test
    fun shouldParseOperationWithPlusAndMinusAndAsteriskAndSlash() {
        initLexer("1 + 2 - 3 * 4 / 5")
        assertEquals(Token.Number, lexer.nextToken().token)
        assertEquals(Token.Whitespace, lexer.nextToken().token)
        assertEquals(Operator.Plus, lexer.nextToken().token)
        assertEquals(Token.Whitespace, lexer.nextToken().token)
        assertEquals(Token.Number, lexer.nextToken().token)
        assertEquals(Token.Whitespace, lexer.nextToken().token)
        assertEquals(Operator.Minus, lexer.nextToken().token)
        assertEquals(Token.Whitespace, lexer.nextToken().token)
        assertEquals(Token.Number, lexer.nextToken().token)
        assertEquals(Token.Whitespace, lexer.nextToken().token)
        assertEquals(Operator.Asterisk, lexer.nextToken().token)
        assertEquals(Token.Whitespace, lexer.nextToken().token)
        assertEquals(Token.Number, lexer.nextToken().token)
        assertEquals(Token.Whitespace, lexer.nextToken().token)
        assertEquals(Operator.Slash, lexer.nextToken().token)
        assertEquals(Token.Whitespace, lexer.nextToken().token)
        assertEquals(Token.Number, lexer.nextToken().token)
        assertEquals(Token.EndOfFile, lexer.nextToken().token)
    }

    @Test
    fun shouldParseOperationWithParenthesis() {
        initLexer("(1 + 2) - (3 * 4) / 5")
        assertEquals(Operator.OpenParenthesis, lexer.nextToken().token)
        assertEquals(Token.Number, lexer.nextToken().token)
        assertEquals(Token.Whitespace, lexer.nextToken().token)
        assertEquals(Operator.Plus, lexer.nextToken().token)
        assertEquals(Token.Whitespace, lexer.nextToken().token)
        assertEquals(Token.Number, lexer.nextToken().token)
        assertEquals(Operator.CloseParenthesis, lexer.nextToken().token)
        assertEquals(Token.Whitespace, lexer.nextToken().token)
        assertEquals(Operator.Minus, lexer.nextToken().token)
        assertEquals(Token.Whitespace, lexer.nextToken().token)
        assertEquals(Operator.OpenParenthesis, lexer.nextToken().token)
        assertEquals(Token.Number, lexer.nextToken().token)
        assertEquals(Token.Whitespace, lexer.nextToken().token)
        assertEquals(Operator.Asterisk, lexer.nextToken().token)
        assertEquals(Token.Whitespace, lexer.nextToken().token)
        assertEquals(Token.Number, lexer.nextToken().token)
        assertEquals(Operator.CloseParenthesis, lexer.nextToken().token)
        assertEquals(Token.Whitespace, lexer.nextToken().token)
        assertEquals(Operator.Slash, lexer.nextToken().token)
        assertEquals(Token.Whitespace, lexer.nextToken().token)
        assertEquals(Token.Number, lexer.nextToken().token)
        assertEquals(Token.EndOfFile, lexer.nextToken().token)
    }

    @Test
    fun shouldParseOperationWithBitwiseAnd() {
        initLexer("1 & 2")
        assertEquals(Token.Number, lexer.nextToken().token)
        assertEquals(Token.Whitespace, lexer.nextToken().token)
        assertEquals(Operator.Ampersand, lexer.nextToken().token)
        assertEquals(Token.Whitespace, lexer.nextToken().token)
        assertEquals(Token.Number, lexer.nextToken().token)
        assertEquals(Token.EndOfFile, lexer.nextToken().token)
    }

    @Test
    fun shouldParseBadToken() {
        initLexer("1 © 2")
        assertEquals(Token.Number, lexer.nextToken().token)
        assertEquals(Token.Whitespace, lexer.nextToken().token)
        assertEquals(Token.Bad, lexer.nextToken().token)
        assertEquals(Token.Whitespace, lexer.nextToken().token)
        assertEquals(Token.Number, lexer.nextToken().token)
        assertEquals(Token.EndOfFile, lexer.nextToken().token)
    }

    @Test
    fun shouldParseExpressionWithUnaryOperator() {
        initLexer("-1")
        assertEquals(Operator.Minus, lexer.nextToken().token)
        assertEquals(Token.Number, lexer.nextToken().token)
        assertEquals(Token.EndOfFile, lexer.nextToken().token)
    }

    @Test
    fun shouldParseExpressionWithParenthesisAndUnaryOperator() {
        initLexer("-(1 + 2)")
        assertEquals(Operator.Minus, lexer.nextToken().token)
        assertEquals(Operator.OpenParenthesis, lexer.nextToken().token)
        assertEquals(Token.Number, lexer.nextToken().token)
        assertEquals(Token.Whitespace, lexer.nextToken().token)
        assertEquals(Operator.Plus, lexer.nextToken().token)
        assertEquals(Token.Whitespace, lexer.nextToken().token)
        assertEquals(Token.Number, lexer.nextToken().token)
        assertEquals(Operator.CloseParenthesis, lexer.nextToken().token)
        assertEquals(Token.EndOfFile, lexer.nextToken().token)
    }
}