package syntax.parser

import syntax.lexer.SyntaxKind
import syntax.lexer.SyntaxToken

class ParenthesizedExpressionSyntax(
    val openParenthesesToken: SyntaxToken<*>,
    val expressionSyntax: ExpressionSyntax,
    val closeParenthesisToken: SyntaxToken<*>
) : ExpressionSyntax() {
    override val kind: SyntaxKind
        get() = SyntaxKind.ParenthesizedExpression

    override fun getChildren(): Iterator<SyntaxNode> {
        return iterator {
            yield(openParenthesesToken)
            yield(expressionSyntax)
            yield(closeParenthesisToken)
        }
    }
}