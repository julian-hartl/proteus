package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Keyword
import lang.proteus.syntax.lexer.token.Operator
import lang.proteus.syntax.lexer.token.Token

internal class TypeSyntax(
    val pointer: PointerSyntax?,
    val typeIdentifier: SyntaxToken<Token.Identifier>,
    syntaxTree: SyntaxTree,
) : SyntaxNode(syntaxTree) {
    override val token: Token
        get() = Token.Type

    val isPointer: Boolean
        get() = pointer != null
}

internal class PointerSyntax(
    val pointer: SyntaxToken<Operator.Ampersand>,
    val mutability: SyntaxToken<Keyword.Mut>?,
    syntaxTree: SyntaxTree,
) : SyntaxNode(syntaxTree) {
    override val token: Token
        get() = Token.Pointer
}