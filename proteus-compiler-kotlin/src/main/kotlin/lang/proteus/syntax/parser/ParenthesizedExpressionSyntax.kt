package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.Token

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