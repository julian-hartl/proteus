package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Token
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
            assertTrue(expression is MemberSyntax, "root expression must be a member syntax statement, but was ${expression::class.simpleName}")

            val iterator = flattenedTree.iterator()
            return AssertingEnumerator(flattenedTree, iterator);
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


    fun <T : SyntaxNode> assertNode(expressionClass: KClass<T>, value: Any? = null): T {
        assertTrue(iterator.hasNext(), "Expected an expression, but found nothing")
        val next = iterator.next()
        val isCorrectExpression = expressionClass.isInstance(next)
        println(flattenedTree)
        assertTrue(
            isCorrectExpression,
            "Expected ${expressionClass.simpleName} but got ${next::class.simpleName}\nTree: $flattenedTree"
        )
        if (value != null) {
            assertEquals(value, (next as LiteralExpressionSyntax).value, "Expected value $value")
        }
        return next as T

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