package lang.proteus.syntax.lexer

import lang.proteus.diagnostics.DiagnosticsBag
import lang.proteus.diagnostics.TextLocation
import lang.proteus.diagnostics.TextSpan
import lang.proteus.syntax.lexer.token.Token
import lang.proteus.syntax.lexer.token_lexers.*
import lang.proteus.syntax.parser.SyntaxTree
import lang.proteus.text.SourceText

internal object Lexers {
    val allLexers: List<TokenLexer>
        get() = listOf(
            WhiteSpaceTokenLexer,
            NumberTokenLexer,
            LetterTokenLexer,
            NonLetterTokenLexer,
            EndOfFileTokenLexer,
        )
}

internal class Lexer(
    private val syntaxTree: SyntaxTree,
) {

    private var position: Int = 0
    val diagnosticsBag: DiagnosticsBag = DiagnosticsBag()

    private val sourceText: SourceText
        get() = syntaxTree.sourceText


    private val tokenLexers: List<TokenLexer> = Lexers.allLexers

    fun nextToken(): SyntaxToken<*> {

        if (current == '"') {
            return readString();
        }
        val matchingLexer = tokenLexers.firstOrNull { it.match(current) }
        if (matchingLexer != null) {
            var length = 0
            val start = position
            val literal = StringBuilder()
            while (matchingLexer.match(current) && length < (matchingLexer.maxLength ?: Int.MAX_VALUE)) {
                length++
                literal.append(current)
                next()
            }
            val tokenLexerResult = matchingLexer.submit(start, position, literal.toString(), syntaxTree)
            if (tokenLexerResult != null) {
                val token = tokenLexerResult.syntaxToken
                val difference = length - tokenLexerResult.consumed
                position -= difference
                return token
            }
        }

        val span = TextSpan(position, 1)
        val location = TextLocation(sourceText, span)
        diagnosticsBag.reportBadCharacter(current, location)
        next()
        return SyntaxToken.badToken(position, current.toString(), syntaxTree)
    }

    private val isAtEnd: Boolean
        get() = position >= sourceText.length

    private fun readString(): SyntaxToken<*> {

        val start = position
        next()
        val literal = StringBuilder()
        var done = false
        while (!done && !isAtEnd) {

            when (current) {
                '"' -> {
                    done = true
                    next()
                }

                '\n', '\r' -> {
                    val span = TextSpan(start, position - start)
                    val location = TextLocation(sourceText, span)
                    diagnosticsBag.reportUnterminatedString(location)
                    done = true
                }

                '\\' -> {
                    next()
                    when (current) {
                        '"' -> literal.append('"')
                        '\\' -> literal.append('\\')
                        'n' -> literal.append('\n')
                        'r' -> literal.append('\r')
                        't' -> literal.append('\t')
                        else -> {
                            val span = TextSpan(start, position - start)
                            val location = TextLocation(sourceText, span)
                            diagnosticsBag.reportIllegalEscape(current, location)
                        }
                    }
                    next()
                }

                else -> {
                    literal.append(current)
                    next()
                }
            }
        }
        if (!done) {
            val span = TextSpan(start, position - start)
            val location = TextLocation(sourceText, span)
            diagnosticsBag.reportUnterminatedString(location)
        }
        return Token.String.toSyntaxToken(start + 1, literal.toString(), syntaxTree = syntaxTree)

    }


    private fun next() {
        position++
    }

    private fun peek(offset: Int): Char {
        val index = position + offset
        if (index >= sourceText.length) {
            return '\u0000'
        }
        return sourceText[index]
    }

    private val current: Char
        get() = peek(0)


}