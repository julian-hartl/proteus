package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Token

class FunctionReturnTypeSyntax(val arrowRight: SyntaxToken<Token.Arrow>, val type: SyntaxToken<Token.Type>) : SyntaxNode() {
    override val token: Token
        get() = Token.FunctionReturnType
}