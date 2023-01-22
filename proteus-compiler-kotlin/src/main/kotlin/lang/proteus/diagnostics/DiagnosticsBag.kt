package lang.proteus.diagnostics

import lang.proteus.symbols.FunctionSymbol
import lang.proteus.symbols.TypeSymbol
import lang.proteus.syntax.lexer.token.Token

internal class DiagnosticsBag {
    private val mutableDiagnostics = MutableDiagnostics()

    fun reportInvalidNumber(literal: String, span: TextSpan, type: TypeSymbol) {
        report("Invalid literal for type $type: '${literal}'", span)
    }

    fun reportBadCharacter(character: Char, position: Int) {
        report("Bad character '${character}'", TextSpan.fromLiteral(position, character.toString()))
    }

    fun reportUnexpectedToken(span: TextSpan, actual: Token, expected: Token) {
        report("Unexpected token <${actual}>, expected <${expected}>", span)
    }

    fun reportBinaryOperatorMismatch(literal: String, span: TextSpan, leftType: TypeSymbol, rightType: TypeSymbol) {
        report(
            "Operator '${literal}' is not defined for types '${leftType}' and '${rightType}'",
            span
        )
    }

    fun reportUnaryOperatorMismatch(literal: String, span: TextSpan, type: TypeSymbol) {
        report("Operator '${literal}' is not defined for type '${type}'", span)
    }

    fun reportUnresolvedReference(span: TextSpan, name: String) {
        report("Unresolved reference: $name", span)
    }

    private fun report(message: String, span: TextSpan) {
        mutableDiagnostics.add(Diagnostic(message, span))
    }

    fun concat(other: DiagnosticsBag) {
        mutableDiagnostics.concat(other.mutableDiagnostics)
    }

    fun reportCannotConvert(span: TextSpan, expectedType: TypeSymbol, actualType: TypeSymbol) {
        report("Cannot convert type '${actualType}' to '${expectedType}'", span)
    }

    fun reportVariableAlreadyDeclared(span: TextSpan, variableName: String) {
        report("Variable '$variableName' already declared", span)
    }

    fun reportFinalVariableCannotBeReassigned(span: TextSpan, variableName: String) {
        report("Val cannot be reassigned", span)
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

    fun reportUnterminatedString(start: Int) {
        report("Unterminated string literal", TextSpan(start, 1))
    }

    fun reportIllegalEscape(current: Char, position: Int) {
        report("Illegal escape sequence '$current'", TextSpan.fromLiteral(position, current.toString()))
    }


    val diagnostics: Diagnostics
        get() = mutableDiagnostics

    fun distinctDiagnostics(): Diagnostics {
        return mutableDiagnostics.distinct()
    }

    fun addAll(diagnostics: Diagnostics) {
        for (diagnostic in diagnostics.diagnostics) {
            mutableDiagnostics.add(diagnostic)
        }
    }

    fun reportUndefinedFunction(span: TextSpan, literal: String) {
        report("Undefined function '$literal'", span)
    }

    fun reportTooFewArguments(span: TextSpan, literal: String, size: Int, count: Int) {
        report("Too few arguments for function '$literal'. Expected $size, got $count", span)
    }

    fun reportTooManyArguments(span: TextSpan, literal: String, size: Int, count: Int) {
        report("Too many arguments for function '$literal'. Expected $size, got $count", span)
    }

    fun reportExpressionMustHaveValue(span: TextSpan) {
        report("Expression must have a value", span)
    }

    fun reportUndefinedType(span: TextSpan, literal: String) {
        report("Undefined type '$literal'", span)
    }

    fun reportParameterAlreadyDeclared(span: TextSpan, name: String) {
        report("Parameter '$name' already declared", span)
    }

    fun reportFunctionAlreadyDeclared(span: TextSpan, literal: String) {
        report("Function '$literal' already declared", span)
    }

    fun reportMainMustHaveNoParameters(mainFunction: FunctionSymbol) {
        report("Main function must have no parameters", mainFunction.declaration!!.span())
    }

    fun reportInvalidTopLevelStatement(span: TextSpan) {
        report("Invalid top-level statement", span)
    }

    fun reportExpectedGlobalStatement() {
        report("Expected a global statement", TextSpan(0, 0))
    }

    fun reportContinueOutsideLoop(span: TextSpan) {
        report("Continue statement must be inside a loop", span)
    }

    fun reportBreakOutsideLoop(span: TextSpan) {
        report("Break statement must be inside a loop", span)
    }

    fun reportInvalidReturnType(textSpan: TextSpan, functionReturnType: TypeSymbol, actualReturnType: TypeSymbol) {
        report("Invalid return type. Expected '${functionReturnType}', got '${actualReturnType}'", textSpan)
    }

    fun reportReturnNotAllowed(span: TextSpan) {
        report("Return statement not allowed here", span)
    }

}