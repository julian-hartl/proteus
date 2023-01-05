package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.Token

class CompilationUnitSyntax(
    val expression: ExpressionSyntax,
    val endOfFileToken: SyntaxToken<Token.EndOfFile>,
) :
    SyntaxNode() {
    override val token: Token
        get() = Token.Expression

}