package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Operator
import lang.proteus.syntax.lexer.token.Token

internal class TypeSyntax(
    val pointer: SyntaxToken<Operator.Ampersand>?,
    val typeIdentifier: SyntaxToken<Token.Identifier>,
    syntaxTree: SyntaxTree,
) : SyntaxNode(syntaxTree) {
    override val token: Token
        get() = Token.Type

    val isPointer: Boolean
        get() = pointer != null
}