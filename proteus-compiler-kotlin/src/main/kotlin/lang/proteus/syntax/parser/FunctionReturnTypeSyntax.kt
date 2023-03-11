package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Token

internal class FunctionReturnTypeSyntax(val arrowRight: SyntaxToken<Token.Arrow>, val type: SyntaxToken<Token.Identifier>,
                               syntaxTree: SyntaxTree
) : SyntaxNode(syntaxTree) {
    override val token: Token
        get() = Token.FunctionReturnType
}