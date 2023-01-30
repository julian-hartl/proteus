package lang.proteus.syntax.lexer.token_lexers

import lang.proteus.syntax.lexer.token.Token
import lang.proteus.syntax.parser.SyntaxTree

internal object AnnotationTokenLexer : TokenLexer() {

    private var hasSeenAt = false

    override fun match(current: Char): Boolean {
        if (current == '@') {
            hasSeenAt = true
            return true
        }
        return hasSeenAt
    }

    override fun submit(start: Int, position: Int, literal: String, syntaxTree: SyntaxTree): TokenLexerResult? {
        if (hasSeenAt) {
            hasSeenAt = false
            return TokenLexerResult(
                Token.Annotation.toSyntaxToken(position, literal, syntaxTree = syntaxTree),
                literal.length
            )
        }
        return null
    }

}