package lang.proteus.diagnostics

import lang.proteus.binding.ProteusType
import lang.proteus.syntax.lexer.Token

class DiagnosticsBag {
    private val mutableDiagnostics = MutableDiagnostics()

    fun reportInvalidNumber(literal: String, span: TextSpan, type: ProteusType) {
        report("Invalid literal for type $type: '${literal}'", span)
    }

    fun reportUnexpectedCharacter(character: Char, position: Int) {
        report("Bad character '${character}'", TextSpan.fromLiteral(position, character.toString()))
    }

    fun reportUnexpectedToken(span: TextSpan, actual: Token, expected: Token) {
        report("Unexpected token <${actual}>, expected <${expected}>", span)
    }

    fun reportBinaryOperatorMismatch(literal: String, span: TextSpan, leftType: ProteusType, rightType: ProteusType) {
        report(
            "Operator '${literal}' cannot be applied to '${leftType}' and '${rightType}'",
            span
        )
    }

    fun reportUnaryOperatorMismatch(literal: String, span: TextSpan, type: ProteusType) {
        report("Operator '${literal}' cannot be applied to '${type}'", span)
    }

    fun reportUndefinedName(span: TextSpan, name: String) {
        report("Undefined name '$name'", span)
    }

    private fun report(message: String, span: TextSpan) {
        mutableDiagnostics.add(Diagnostic("ERROR: $message at position ${span.start}", span))
    }

    fun concat(other: DiagnosticsBag) {
        mutableDiagnostics.concat(other.mutableDiagnostics)
    }

    fun reportCannotAssign(span: TextSpan, currentType: ProteusType, newType: ProteusType) {
        report("Cannot assign '${newType}' to '${currentType}'", span)
    }


    val diagnostics: Diagnostics
        get() = mutableDiagnostics
}