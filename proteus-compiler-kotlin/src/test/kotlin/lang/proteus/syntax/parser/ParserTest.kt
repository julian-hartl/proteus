package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.Operator
import lang.proteus.syntax.lexer.Operators
import lang.proteus.syntax.lexer.Token
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class ParserTest {

    @Test
    fun `should allow empty string`() {
        val expression = SyntaxTree.parse("\"\"")
        val e = AssertingEnumerator.fromExpression(expression.root.statement)
        e.assertExpression(LiteralExpressionSyntax::class, "");
        e.dispose()


    }

    @ParameterizedTest(name = "Precedence: a {0} b {1} c")
    @MethodSource
    fun `parser BinaryExpression honors precedences`(op1: Operator, op2: Operator) {
        val precedence1 = op1.precedence
        val precedence2 = op2.precedence
        val text = "a ${op1.literal} b ${op2.literal} c"
        val expression = SyntaxTree.parse(text)
        if (expression.hasErrors()) return;

        val e = AssertingEnumerator.fromExpression(expression.root.statement)
        if (precedence1 >= precedence2) {
            e.assertExpression(BinaryExpressionSyntax::class)
            e.assertExpression(BinaryExpressionSyntax::class)
            e.assertExpression(NameExpressionSyntax::class)
            e.assertToken(Token.Identifier, "a")
            e.assertToken(op1, op1.literal)
            e.assertExpression(NameExpressionSyntax::class)
            e.assertToken(Token.Identifier, "b")
            e.assertToken(op2, op2.literal)
            e.assertExpression(NameExpressionSyntax::class)
            e.assertToken(Token.Identifier, "c")
            e.dispose()

        } else {
            e.assertExpression(BinaryExpressionSyntax::class)
            e.assertExpression(NameExpressionSyntax::class)
            e.assertToken(Token.Identifier, "a")
            e.assertToken(op1, op1.literal)
            e.assertExpression(BinaryExpressionSyntax::class)
            e.assertExpression(NameExpressionSyntax::class)
            e.assertToken(Token.Identifier, "b")
            e.assertToken(op2, op2.literal)
            e.assertExpression(NameExpressionSyntax::class)
            e.assertToken(Token.Identifier, "c")
            e.dispose()
        }

    }

    @ParameterizedTest(name = "Precedence: a {0} b {1} c")
    @MethodSource
    fun `parser UnaryExpression honors precedences`(unaryOperator: Operator, binaryOperator: Operator) {
        val unaryPrecedence = unaryOperator.precedence
        val binaryPrecedence = binaryOperator.precedence
        val text = "a ${unaryOperator.literal} b ${binaryOperator.literal}"
        val expression = SyntaxTree.parse(text)
        if (expression.hasErrors()) return;

        val e = AssertingEnumerator.fromExpression(expression.root)
        if (unaryPrecedence >= binaryPrecedence) {
            e.assertExpression(BinaryExpressionSyntax::class)
            e.assertExpression(UnaryExpressionSyntax::class)
            e.assertToken(unaryOperator, unaryOperator.literal)
            e.assertExpression(NameExpressionSyntax::class)
            e.assertToken(Token.Identifier, "a")
            e.assertToken(unaryOperator, unaryOperator.literal)
            e.assertExpression(NameExpressionSyntax::class)
            e.assertToken(Token.Identifier, "b")
            e.dispose()

        } else {
            e.assertExpression(UnaryExpressionSyntax::class)
            e.assertToken(unaryOperator, unaryOperator.literal)
            e.assertExpression(BinaryExpressionSyntax::class)
            e.assertExpression(NameExpressionSyntax::class)
            e.assertToken(Token.Identifier, "a")
            e.assertExpression(NameExpressionSyntax::class)
            e.assertToken(Token.Identifier, "b")
            e.dispose()
        }

    }

    companion object {
        @JvmStatic
        fun `parser BinaryExpression honors precedences`(): Stream<Arguments> {
            val pairs = mutableListOf<Arguments>()
            for (op1 in Operators.binaryOperators) {
                for (op2 in Operators.binaryOperators) {
                    pairs.add(Arguments.of(op1, op2))
                }
            }
            return pairs.stream()
        }

        @JvmStatic
        fun `parser UnaryExpression honors precedences`(): Stream<Arguments> {
            val pairs = mutableListOf<Arguments>()
            for (op1 in Operators.unaryOperators) {
                for (op2 in Operators.binaryOperators) {
                    pairs.add(Arguments.of(op1, op2))
                }
            }
            return pairs.stream()
        }
    }

}