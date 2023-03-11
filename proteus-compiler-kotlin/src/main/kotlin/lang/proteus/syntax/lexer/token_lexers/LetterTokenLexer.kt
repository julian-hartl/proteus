package lang.proteus.syntax.lexer.token_lexers

import lang.proteus.symbols.TypeSymbol
import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Keyword
import lang.proteus.syntax.lexer.token.Operators
import lang.proteus.syntax.lexer.token.Token
import lang.proteus.syntax.parser.SyntaxTree

internal object LetterTokenLexer : TokenLexer() {
    var length = 0

    override fun match(current: Char): Boolean {
        if(length == 0) {
            length++
            return current.isLetter()
        }
        length++
        return current.isLetter() || current.isDigit() || current == '_'
    }

    override fun submit(start: Int, position: Int, literal: String, syntaxTree: SyntaxTree): TokenLexerResult {
        val token = syntaxToken(literal, start, syntaxTree)
        length = 0
        return TokenLexerResult(token, literal.length)
    }

    private fun syntaxToken(
        literal: String,
        start: Int, syntaxTree: SyntaxTree,
    ): SyntaxToken<out Token> {
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