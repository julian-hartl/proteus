package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Keyword
import lang.proteus.syntax.lexer.token.Operator
import lang.proteus.syntax.lexer.token.Token
import lang.proteus.syntax.parser.statements.BlockStatementSyntax

internal class FunctionDeclarationSyntax(
    val modifiers: Set<Keyword>,
    val functionKeyword: SyntaxToken<Keyword.Fn>,
    val identifier: SyntaxToken<Token.Identifier>,
    val openParenthesisToken: SyntaxToken<Operator.OpenParenthesis>,
    val parameters: SeparatedSyntaxList<ParameterSyntax>,
    val closeParenthesisToken: SyntaxToken<Operator.CloseParenthesis>,
    val returnTypeClause: FunctionReturnTypeSyntax?,
    val body: BlockStatementSyntax?,
    val semiColon: SyntaxToken<Token.SemiColon>?,
    syntaxTree: SyntaxTree,
) : MemberSyntax(syntaxTree) {
    override val token: Token
        get() = Token.FunctionDeclaration

    val isExternal get() = modifiers.contains(Keyword.External)
}