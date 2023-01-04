package syntax.parser

import syntax.lexer.SyntaxToken
import syntax.lexer.Token

class ParenthesizedExpressionSyntax(
    val openParenthesesToken: SyntaxToken<*>,
    val expressionSyntax: ExpressionSyntax,
    val closeParenthesisToken: SyntaxToken<*>
) : ExpressionSyntax() {
    override val token: Token
        get() = expressionSyntax.token

    override fun getChildren(): Iterator<SyntaxNode> {
        return iterator {
            yield(openParenthesesToken)
            yield(expressionSyntax)
            yield(closeParenthesisToken)
        }
    }
}