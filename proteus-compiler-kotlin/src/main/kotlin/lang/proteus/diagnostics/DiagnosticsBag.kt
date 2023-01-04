package lang.proteus.diagnostics

import lang.proteus.binding.BoundType
import lang.proteus.syntax.lexer.Token

class DiagnosticsBag {
    private val mutableDiagnostics = MutableDiagnostics()

    private fun report(message: String, span: TextSpan) {
        mutableDiagnostics.add(Diagnostic("ERROR: $message at ${span.start}", span))
    }

    fun reportInvalidNumber(span: TextSpan, type: BoundType) {
        report("Invalid literal for type $type: '${span.literal}'", span)
    }

    fun reportUnexpectedCharacter(character: Char, position: Int) {
        report("Bad character '${character}'", TextSpan(position, character.toString()))
    }

    fun reportUnexpectedToken(span: TextSpan, actual: Token, expected: Token) {
        report("Unexpected token <${actual}>, expected <${expected}>", span)
    }

    fun concat(other: DiagnosticsBag) {
        mutableDiagnostics.concat(other.mutableDiagnostics)
    }

    fun reportBinaryOperatorMismatch(span: TextSpan, leftType: BoundType, rightType: BoundType) {
        report(
            "Operator '${span.literal}' cannot be applied to '${leftType}' and '${rightType}'",
            span
        )
    }

    fun reportUnaryOperatorMismatch(span: TextSpan, type: BoundType) {
        report("Operator '${span.literal}' cannot be applied to '${type}'", span)
    }

    val diagnostics: Diagnostics
        get() = mutableDiagnostics
}