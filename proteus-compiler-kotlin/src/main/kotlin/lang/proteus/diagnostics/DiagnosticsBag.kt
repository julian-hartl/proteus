package lang.proteus.diagnostics

import lang.proteus.binding.ProteusType
import lang.proteus.syntax.lexer.Token

class DiagnosticsBag {
    private val mutableDiagnostics = MutableDiagnostics()

    fun reportInvalidNumber(span: TextSpan, type: ProteusType) {
        report("Invalid literal for type $type: '${span.literal}'", span)
    }

    fun reportUnexpectedCharacter(character: Char, position: Int) {
        report("Bad character '${character}'", TextSpan(position, character.toString()))
    }

    fun reportUnexpectedToken(span: TextSpan, actual: Token, expected: Token) {
        report("Unexpected token <${actual}>, expected <${expected}>", span)
    }

    fun reportBinaryOperatorMismatch(span: TextSpan, leftType: ProteusType, rightType: ProteusType) {
        report(
            "Operator '${span.literal}' cannot be applied to '${leftType}' and '${rightType}'",
            span
        )
    }

    fun reportUnaryOperatorMismatch(span: TextSpan, type: ProteusType) {
        report("Operator '${span.literal}' cannot be applied to '${type}'", span)
    }

    private fun report(message: String, span: TextSpan) {
        mutableDiagnostics.add(Diagnostic("ERROR: $message at position ${span.start}", span))
    }

    fun concat(other: DiagnosticsBag) {
        mutableDiagnostics.concat(other.mutableDiagnostics)
    }

    val diagnostics: Diagnostics
        get() = mutableDiagnostics
}