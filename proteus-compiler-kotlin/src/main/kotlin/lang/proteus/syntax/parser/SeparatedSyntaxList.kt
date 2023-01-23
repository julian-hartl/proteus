package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Token
import kotlin.math.min

internal data class SeparatedSyntaxList<T : SyntaxNode>(val separatorsAndNodes: List<SyntaxNode>) : Iterable<T> {
    val count: Int = min(separatorsAndNodes.size, separatorsAndNodes.count() / 2 + 1)

    fun get(index: Int): T {
        return separatorsAndNodes[index * 2] as T
    }

    fun getSeparator(index: Int): SyntaxToken<Token.Comma> {
        return separatorsAndNodes[index * 2 + 1] as SyntaxToken<Token.Comma>
    }

    fun getNodes(): List<T> {
        return iterator().asSequence().toList()
    }

    override fun iterator(): Iterator<T> {
        return iterator {
            for (i in 0 until count) {
                yield(get(i) as T)
            }
        }
    }
}