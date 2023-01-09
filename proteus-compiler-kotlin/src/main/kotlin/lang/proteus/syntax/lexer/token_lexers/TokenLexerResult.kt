package lang.proteus.syntax.lexer.token_lexers

import lang.proteus.syntax.lexer.SyntaxToken

internal data class TokenLexerResult(val syntaxToken: SyntaxToken<*>, val consumed: Int)
