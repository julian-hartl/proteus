package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.Token
import lang.proteus.syntax.parser.statements.ExpressionStatementSyntax
import java.util.*
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class AssertingEnumerator<T> private constructor(
    private val flattenedTree: List<SyntaxNode>,
    private val iterator: Iterator<SyntaxNode>,
) {


    companion object {
        fun fromExpression(expression: SyntaxNode): AssertingEnumerator<SyntaxNode> {
            val flattenedTree = flatten(expression)
            assertTrue(expression is ExpressionStatementSyntax, "root expression must be an expression statement")
            return AssertingEnumerator(flattenedTree, flattenedTree.iterator());
        }

        private fun flatten(node: SyntaxNode): List<SyntaxNode> {
            val stack = Stack<SyntaxNode>()
            stack.push(node)

            val result = mutableListOf<SyntaxNode>()
            while (stack.isNotEmpty()) {
                val current = stack.pop()
                result.add(current)
                for (child in current.getChildren().asReversed()) {
                    stack.push(child)
                }
            }
            return result
        }
    }


    fun <T : ExpressionSyntax> assertExpression(expressionClass: KClass<T>, value: Any? = null) {
        assertTrue(iterator.hasNext(), "Expected an expression, but found nothing")
        val next = iterator.next()
        val isCorrectExpression = expressionClass.isInstance(next)
        assertTrue(
            isCorrectExpression,
            "Expected ${expressionClass.simpleName} but got ${next::class.simpleName}\nTree: $flattenedTree"
        )
        if (value != null) {
            assertEquals(value, (next as LiteralExpressionSyntax).value, "Expected value $value")
        }

    }

    fun assertToken(token: Token, literal: String) {
        assertTrue(iterator.hasNext(), "Expected a token, but found nothing")
        val next = iterator.next()
        assertEquals(token, next.token, "Expected token $token but got ${next.token}")
        if (next is SyntaxToken<*>) {
            assertEquals(literal, next.literal, "Expected literal $literal but got ${next.literal}")
        }
    }

    fun dispose() {
        assertTrue(
            !iterator.hasNext(),
            if (!iterator.hasNext()) "" else "Expected no more tokens, but found ${iterator.next()}"
        )
    }
}