package parser

import syntax.lexer.SyntaxKind
import org.junit.jupiter.api.Test
import syntax.parser.Parser
import kotlin.test.assertEquals

class ParserTest {
    private lateinit var parser: Parser

    private fun initParser(input: String) {
        parser = Parser(input)
    }

    @Test
    fun shouldParseBasicPlusOperation() {
        initParser("1 + 2")
        val ast = parser.parse()
        assertEquals(SyntaxKind.BinaryExpression, ast.root.kind)
        val rootChildren = ast.root.getChildren()
        assertEquals(SyntaxKind.LiteralExpression, rootChildren.next().kind)
        assertEquals(SyntaxKind.PlusToken, rootChildren.next().kind)
        assertEquals(SyntaxKind.LiteralExpression, rootChildren.next().kind)
    }

    @Test
    fun shouldParseOperationWithPlusAndMinus() {
        initParser("1 + 2 - 3")
        val ast = parser.parse()
        assertEquals(SyntaxKind.BinaryExpression, ast.root.kind)
        val rootChildren = ast.root.getChildren()
        val plusNode = rootChildren.next()
        assertEquals(SyntaxKind.BinaryExpression, plusNode.kind)
        val plusNodeChildren = plusNode.getChildren()
        assertEquals(SyntaxKind.LiteralExpression, plusNodeChildren.next().kind)
        assertEquals(SyntaxKind.PlusToken, plusNodeChildren.next().kind)
        assertEquals(SyntaxKind.LiteralExpression, plusNodeChildren.next().kind)
        assertEquals(SyntaxKind.MinusToken, rootChildren.next().kind)
        assertEquals(SyntaxKind.LiteralExpression, rootChildren.next().kind)
    }

    @Test
    fun shouldParseOperationWithPlusAndMinusAndAsterisk() {
        initParser("1 + 2 - 3 * 4")
        val ast = parser.parse()
        assertEquals(SyntaxKind.BinaryExpression, ast.root.kind)
        val rootChildren = ast.root.getChildren()
        val plusNode = rootChildren.next()
        assertEquals(SyntaxKind.BinaryExpression, plusNode.kind)
        val plusNodeChildren = plusNode.getChildren()
        assertEquals(SyntaxKind.LiteralExpression, plusNodeChildren.next().kind)
        assertEquals(SyntaxKind.PlusToken, plusNodeChildren.next().kind)
        assertEquals(SyntaxKind.LiteralExpression, plusNodeChildren.next().kind)
        assertEquals(SyntaxKind.MinusToken, rootChildren.next().kind)
        val asteriskNode = rootChildren.next()
        assertEquals(SyntaxKind.BinaryExpression, asteriskNode.kind)
        val asteriskNodeChildren = asteriskNode.getChildren()
        assertEquals(SyntaxKind.LiteralExpression, asteriskNodeChildren.next().kind)
        assertEquals(SyntaxKind.AsteriskToken, asteriskNodeChildren.next().kind)
        assertEquals(SyntaxKind.LiteralExpression, asteriskNodeChildren.next().kind)
    }

    @Test
    fun shouldParseOperationWithParenthesis() {
        initParser("(1 + 2) - 3 * 4")
        val ast = parser.parse()
        assertEquals(SyntaxKind.BinaryExpression, ast.root.kind)
        val rootChildren = ast.root.getChildren()
        val parenthesisNode = rootChildren.next()
        assertEquals(SyntaxKind.ParenthesizedExpression, parenthesisNode.kind)
        val parenthesisChildren = parenthesisNode.getChildren()

        assertEquals(SyntaxKind.OpenParenthesisToken, parenthesisChildren.next().kind)
        val plusNode = parenthesisChildren.next()
        assertEquals(SyntaxKind.BinaryExpression, plusNode.kind)
        val plusNodeChildren = plusNode.getChildren()
        assertEquals(SyntaxKind.LiteralExpression, plusNodeChildren.next().kind)
        assertEquals(SyntaxKind.PlusToken, plusNodeChildren.next().kind)
        assertEquals(SyntaxKind.LiteralExpression, plusNodeChildren.next().kind)
        assertEquals(SyntaxKind.CloseParenthesisToken, parenthesisChildren.next().kind)
        assertEquals(SyntaxKind.MinusToken, rootChildren.next().kind)
        val asteriskNode = rootChildren.next()
        assertEquals(SyntaxKind.BinaryExpression, asteriskNode.kind)
        val asteriskNodeChildren = asteriskNode.getChildren()
        assertEquals(SyntaxKind.LiteralExpression, asteriskNodeChildren.next().kind)
        assertEquals(SyntaxKind.AsteriskToken, asteriskNodeChildren.next().kind)
        assertEquals(SyntaxKind.LiteralExpression, asteriskNodeChildren.next().kind)
    }

    @Test
    fun shouldParseExpressionWithUnaryOperator() {
        initParser("-1 + 2")
        val ast = parser.parse()
        assertEquals(SyntaxKind.BinaryExpression, ast.root.kind)
        val rootChildren = ast.root.getChildren()
        val unaryNode = rootChildren.next()
        assertEquals(SyntaxKind.UnaryExpression, unaryNode.kind)
        val unaryNodeChildren = unaryNode.getChildren()
        assertEquals(SyntaxKind.MinusToken, unaryNodeChildren.next().kind)
        assertEquals(SyntaxKind.LiteralExpression, unaryNodeChildren.next().kind)
        assertEquals(SyntaxKind.PlusToken, rootChildren.next().kind)
        assertEquals(SyntaxKind.LiteralExpression, rootChildren.next().kind)
    }

}