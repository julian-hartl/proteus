package parser

import org.junit.jupiter.api.Test
import de.proteus.syntax.lexer.Operator
import de.proteus.syntax.parser.*
import de.proteus.syntax.lexer.Token
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParserTest {
    private lateinit var parser: Parser

    private fun initParser(input: String) {
        parser = Parser(input)
    }

    @Test
    fun shouldParseBasicPlusOperation() {
        initParser("1 + 2")
        val ast = parser.parse()
        assertTrue(ast.root is BinaryExpressionSyntax)
        val rootChildren = ast.root.getChildren()
        assertTrue(rootChildren.next().token is Token.Number)
        assertEquals(Operator.Plus, rootChildren.next().token)
        assertTrue(rootChildren.next().token is Token.Number)
    }

    @Test
    fun shouldParseOperationWithPlusAndMinus() {
        initParser("1 + 2 - 3")
        val ast = parser.parse()
        assertTrue(ast.root is BinaryExpressionSyntax)
        val rootChildren = ast.root.getChildren()
        val plusNode = rootChildren.next()
        assertTrue(plusNode is BinaryExpressionSyntax)
        val plusNodeChildren = plusNode.getChildren()
        assertTrue(plusNodeChildren.next().token is Token.Number)
        assertEquals(plusNodeChildren.next().token, Operator.Plus)
        assertTrue(plusNodeChildren.next().token is Token.Number)
        assertEquals(Operator.Minus, rootChildren.next().token)
        assertTrue(rootChildren.next().token is Token.Number)
    }

    @Test
    fun shouldParseOperationWithPlusAndMinusAndAsterisk() {
        initParser("1 + 2 - 3 * 4")
        val ast = parser.parse()
        assertTrue(ast.root is BinaryExpressionSyntax)
        val rootChildren = ast.root.getChildren()
        val plusNode = rootChildren.next()
        assertTrue(plusNode is BinaryExpressionSyntax)
        val plusNodeChildren = plusNode.getChildren()
        assertTrue(plusNodeChildren.next().token is Token.Number)
        assertEquals(plusNodeChildren.next().token, Operator.Plus)
        assertTrue(plusNodeChildren.next().token is Token.Number)
        assertEquals(Operator.Minus, rootChildren.next().token)
        val asteriskNode = rootChildren.next()
        assertTrue(asteriskNode is BinaryExpressionSyntax)
        val asteriskNodeChildren = asteriskNode.getChildren()
        assertTrue(asteriskNodeChildren.next() is LiteralExpressionSyntax)
        assertEquals(Operator.Asterisk, asteriskNodeChildren.next().token)
        assertTrue(asteriskNodeChildren.next() is LiteralExpressionSyntax)
    }

    @Test
    fun shouldParseOperationWithParenthesis() {
        initParser("(1 + 2) - 3 * 4")
        val ast = parser.parse()
        assertTrue(ast.root is BinaryExpressionSyntax)
        val rootChildren = ast.root.getChildren()
        val parenthesisNode = rootChildren.next()
        assertTrue(parenthesisNode is ParenthesizedExpressionSyntax)
        val parenthesisChildren = parenthesisNode.getChildren()

        assertEquals(Operator.OpenParenthesis, parenthesisChildren.next().token)
        val plusNode = parenthesisChildren.next()
        assertTrue(plusNode is BinaryExpressionSyntax)
        val plusNodeChildren = plusNode.getChildren()
        assertTrue(plusNodeChildren.next().token is Token.Number)
        assertEquals(plusNodeChildren.next().token, Operator.Plus)
        assertTrue(plusNodeChildren.next().token is Token.Number)
        assertEquals(Operator.CloseParenthesis, parenthesisChildren.next().token)
        assertEquals(Operator.Minus, rootChildren.next().token)
        val asteriskNode = rootChildren.next()
        assertTrue(asteriskNode is BinaryExpressionSyntax)
        val asteriskNodeChildren = asteriskNode.getChildren()
        assertTrue(asteriskNodeChildren.next() is LiteralExpressionSyntax)
        assertEquals(Operator.Asterisk, asteriskNodeChildren.next().token)
        assertTrue(asteriskNodeChildren.next() is LiteralExpressionSyntax)
    }

    @Test
    fun shouldParseExpressionWithUnaryOperator() {
        initParser("-1 + 2")
        val ast = parser.parse()
        assertTrue(ast.root is BinaryExpressionSyntax)
        val rootChildren = ast.root.getChildren()
        val unaryNode = rootChildren.next()
        assertTrue(unaryNode is UnaryExpressionSyntax)
        val unaryNodeChildren = unaryNode.getChildren()
        assertEquals(Operator.Minus, unaryNodeChildren.next().token)
        assertTrue(unaryNodeChildren.next() is LiteralExpressionSyntax)
        assertEquals(Operator.Plus, rootChildren.next().token)
        assertTrue(rootChildren.next().token is Token.Number)
    }

}