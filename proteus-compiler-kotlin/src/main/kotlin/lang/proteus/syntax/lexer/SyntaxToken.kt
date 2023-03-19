package lang.proteus.syntax.lexer

import lang.proteus.diagnostics.TextSpan
import lang.proteus.symbols.TypeSymbol
import lang.proteus.syntax.lexer.token.Operator
import lang.proteus.syntax.lexer.token.Operators
import lang.proteus.syntax.lexer.token.Token
import lang.proteus.syntax.parser.SyntaxNode
import lang.proteus.syntax.parser.SyntaxTree

internal class SyntaxToken<T : Token>(
    override val token: T,
    var position: Int,
    val literal: String,
    val value: Any?,
    syntaxTree: SyntaxTree,
) : SyntaxNode(syntaxTree) {
    override fun span(): TextSpan {
        return TextSpan(position, literal.length)
    }


    companion object {


        fun endOfFile(position: Int, syntaxTree: SyntaxTree): SyntaxToken<Token.EndOfFile> {
            return SyntaxToken(Token.EndOfFile, position, "", null, syntaxTree)
        }


        fun identifierToken(position: Int, literal: String, syntaxTree: SyntaxTree): SyntaxToken<Token.Identifier> {
            return Token.Identifier.toSyntaxToken(
                position,
                literal,
                syntaxTree = syntaxTree
            ) as SyntaxToken<Token.Identifier>
        }

        fun numberToken(position: Int, literal: String, syntaxTree: SyntaxTree): SyntaxToken<Token.Number> {
            return Token.Number.toSyntaxToken(
                position,
                literal,
                literal.toIntOrNull(),
                syntaxTree
            ) as SyntaxToken<Token.Number>
        }

        fun whiteSpaceToken(position: Int, literal: String, syntaxTree: SyntaxTree): SyntaxToken<Token.Whitespace> {
            return Token.Whitespace.toSyntaxToken(position, literal, null, syntaxTree) as SyntaxToken<Token.Whitespace>
        }

        fun operator(position: Int, operatorLiteral: String, syntaxTree: SyntaxTree): SyntaxToken<Operator>? {
            val operator = Operators.fromLiteral(operatorLiteral) ?: return null
            return operator.toSyntaxToken(position, syntaxTree)
        }

        fun badToken(position: Int, literal: String, syntaxTree: SyntaxTree): SyntaxToken<Token.Bad> {
            return Token.Bad.toSyntaxToken(position, literal, syntaxTree = syntaxTree) as SyntaxToken<Token.Bad>
        }
    }


}