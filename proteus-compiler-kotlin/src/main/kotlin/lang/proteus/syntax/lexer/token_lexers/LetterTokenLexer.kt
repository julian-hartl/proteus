package lang.proteus.syntax.lexer.token_lexers

import lang.proteus.symbols.TypeSymbol
import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Keyword
import lang.proteus.syntax.lexer.token.Operators
import lang.proteus.syntax.lexer.token.Token
import lang.proteus.syntax.parser.SyntaxTree

internal object LetterTokenLexer : TokenLexer() {
    override fun match(current: Char): Boolean {
        return current.isLetter()
    }

    override fun submit(start: Int, position: Int, literal: String, syntaxTree: SyntaxTree): TokenLexerResult {
        val token = syntaxToken(literal, start, syntaxTree)
        return TokenLexerResult(token, literal.length)
    }

    private fun syntaxToken(
        literal: String,
        start: Int, syntaxTree: SyntaxTree,
    ): SyntaxToken<out Token> {
        val type = TypeSymbol.fromName(literal)
        if (type != null) {
            return SyntaxToken.typeToken(start, literal, type, syntaxTree)
        }
        val operator = Operators.fromLiteral(literal)
        if (operator != null) {
            return operator.toSyntaxToken(start, syntaxTree)
        }
        val keyword = Keyword.fromLiteral(literal)
        if (keyword != null) {
            return keyword.toSyntaxToken(start, syntaxTree)
        }
        return SyntaxToken.identifierToken(start, literal, syntaxTree)
    }

}