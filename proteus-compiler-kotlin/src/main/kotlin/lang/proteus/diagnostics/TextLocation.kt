package lang.proteus.diagnostics

import lang.proteus.binding.Module
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Token

internal data class TextLocation private constructor(
    val startLine: Int,
    val endLine: Int,
    val startCharacter: Int,
    val endCharacter: Int,
    val fileName: String,
) {

    constructor(module: Module, context: ParserRuleContext) : this(
        context.start.line,
        context.stop.line,
        context.start.charPositionInLine,
        context.stop.charPositionInLine + context.stop.text.length,
        module.moduleReference.name
    )

    constructor(module: Module, token: Token) : this(
        token.line,
        token.line,
        token.charPositionInLine,
        token.charPositionInLine + token.text.length,
        module.moduleReference.name
    )

    override fun toString(): String {
        return if (startLine == endLine) {
            "($startLine, $startCharacter-$endCharacter)"
        } else {
            "($startLine, $startCharacter)-($endLine, $endCharacter)"
        }
    }
}