package lang.proteus.diagnostics

import lang.proteus.binding.ProteusType
import lang.proteus.syntax.lexer.Token

internal class DiagnosticsBag {
    private val mutableDiagnostics = MutableDiagnostics()

    fun reportInvalidNumber(literal: String, span: TextSpan, type: ProteusType) {
        report("Invalid literal for type $type: '${literal}'", span)
    }

    fun reportBadCharacter(character: Char, position: Int) {
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

    fun reportUndeclaredVariable(span: TextSpan, name: String) {
        report("Variable '$name' not declared", span)
    }

    private fun report(message: String, span: TextSpan) {
        mutableDiagnostics.add(Diagnostic("ERROR: $message", span))
    }

    fun concat(other: DiagnosticsBag) {
        mutableDiagnostics.concat(other.mutableDiagnostics)
    }

    fun reportCannotConvert(span: TextSpan, currentType: ProteusType, newType: ProteusType) {
        report("Cannot convert '${newType}' to '${currentType}'", span)
    }

    fun reportVariableAlreadyDeclared(span: TextSpan, variableName: String) {
        report("Variable '$variableName' already declared", span)
    }

    fun reportFinalVariableCannotBeReassigned(span: TextSpan, variableName: String) {
        report("Final variable '$variableName' cannot be reassigned", span)
    }

    fun reportInvalidCharLiteral(literal: String, span: TextSpan) {
        report("Invalid character literal '$literal'", span)
    }

    fun reportInvalidBinaryString(span: TextSpan, binaryString: String) {
        report("Invalid binary string '$binaryString'", span)
    }

    fun reportInvalidNumberStringIdentifier(span: TextSpan, literal: String) {
        report("Invalid number string identifier '$literal'", span)
    }


    val diagnostics: Diagnostics
        get() = mutableDiagnostics
}