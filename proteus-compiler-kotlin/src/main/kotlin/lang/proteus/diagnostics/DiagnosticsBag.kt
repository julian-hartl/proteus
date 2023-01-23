package lang.proteus.diagnostics

import lang.proteus.binding.ImportGraphNode
import lang.proteus.symbols.FunctionSymbol
import lang.proteus.symbols.TypeSymbol
import lang.proteus.syntax.lexer.token.Token
import lang.proteus.syntax.parser.ImportStatementSyntax

internal class DiagnosticsBag {
    private val mutableDiagnostics = MutableDiagnostics()

    fun reportInvalidNumber(literal: String, textLocation: TextLocation, type: TypeSymbol) {
        report("Invalid literal for type $type: '${literal}'", textLocation)
    }

    fun reportBadCharacter(character: Char, textLocation: TextLocation) {
        report("Bad character '${character}'", textLocation)
    }

    fun reportUnexpectedToken(textLocation: TextLocation, actual: Token, expected: Token) {
        report("Unexpected token <${actual}>, expected <${expected}>", textLocation)
    }

    fun reportBinaryOperatorMismatch(
        literal: String,
        textLocation: TextLocation,
        leftType: TypeSymbol,
        rightType: TypeSymbol,
    ) {
        report(
            "Operator '${literal}' is not defined for types '${leftType}' and '${rightType}'",
            textLocation
        )
    }

    fun reportUnaryOperatorMismatch(literal: String, textLocation: TextLocation, type: TypeSymbol) {
        report("Operator '${literal}' is not defined for type '${type}'", textLocation)
    }

    fun reportUnresolvedReference(textLocation: TextLocation, name: String) {
        report("Unresolved reference: $name", textLocation)
    }

    private fun report(message: String, location: TextLocation, diagnosticType: DiagnosticType = DiagnosticType.Error) {
        mutableDiagnostics.add(Diagnostic(message, location, diagnosticType))
    }

    fun concat(other: DiagnosticsBag) {
        mutableDiagnostics.concat(other.mutableDiagnostics)
    }

    fun reportCannotConvert(textLocation: TextLocation, expectedType: TypeSymbol, actualType: TypeSymbol) {
        report("Cannot convert type '${actualType}' to '${expectedType}'", textLocation)
    }

    fun reportVariableAlreadyDeclared(textLocation: TextLocation, variableName: String) {
        report("Variable '$variableName' already declared", textLocation)
    }

    fun reportFinalVariableCannotBeReassigned(textLocation: TextLocation, variableName: String) {
        report("Val cannot be reassigned", textLocation)
    }

    fun reportInvalidCharLiteral(literal: String, textLocation: TextLocation) {
        report("Invalid character literal '$literal'", textLocation)
    }

    fun reportInvalidBinaryString(textLocation: TextLocation, binaryString: String) {
        report("Invalid binary string '$binaryString'", textLocation)
    }

    fun reportInvalidNumberStringIdentifier(textLocation: TextLocation, literal: String) {
        report("Invalid number string identifier '$literal'", textLocation)
    }

    fun reportUnterminatedString(textLocation: TextLocation) {
        report("Unterminated string literal", location = textLocation)
    }

    fun reportIllegalEscape(current: Char, textLocation: TextLocation) {
        report("Illegal escape sequence '$current'", textLocation)
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

    fun reportUndefinedFunction(textLocation: TextLocation, literal: String) {
        report("Undefined function '$literal'", textLocation)
    }

    fun reportTooFewArguments(textLocation: TextLocation, literal: String, size: Int, count: Int) {
        report("Too few arguments for function '$literal'. Expected $size, got $count", textLocation)
    }

    fun reportTooManyArguments(textLocation: TextLocation, literal: String, size: Int, count: Int) {
        report("Too many arguments for function '$literal'. Expected $size, got $count", textLocation)
    }

    fun reportExpressionMustHaveValue(textLocation: TextLocation) {
        report("Expression must have a value", textLocation)
    }

    fun reportUndefinedType(textLocation: TextLocation, literal: String) {
        report("Undefined type '$literal'", textLocation)
    }

    fun reportParameterAlreadyDeclared(textLocation: TextLocation, name: String) {
        report("Parameter '$name' already declared", textLocation)
    }

    fun reportFunctionAlreadyDeclared(textLocation: TextLocation, literal: String) {
        report("Function '$literal' already declared", textLocation)
    }

    fun reportMainMustHaveNoParameters(mainFunction: FunctionSymbol) {
        report("Main function must have no parameters", mainFunction.declaration!!.location)
    }

    fun reportInvalidTopLevelStatement(textLocation: TextLocation) {
        report("Invalid top-level statement", textLocation)
    }

    fun reportExpectedGlobalStatement(textLocation: TextLocation) {
        report("Expected a global statement", textLocation)
    }

    fun reportContinueOutsideLoop(textLocation: TextLocation) {
        report("Continue statement must be inside a loop", textLocation)
    }

    fun reportBreakOutsideLoop(textLocation: TextLocation) {
        report("Break statement must be inside a loop", textLocation)
    }

    fun reportInvalidReturnType(
        textLocation: TextLocation,
        functionReturnType: TypeSymbol,
        actualReturnType: TypeSymbol,
    ) {
        report("Invalid return type. Expected '${functionReturnType}', got '${actualReturnType}'", textLocation)
    }

    fun reportReturnNotAllowed(textLocation: TextLocation) {
        report("Return statement not allowed here", textLocation)
    }

    fun reportAllCodePathsMustReturn(textLocation: TextLocation) {
        report("Not all code paths return a value. Hint: To fix this, you could add a return statement.", textLocation)
    }

    fun reportExpectedConstantExpression(textLocation: TextLocation) {
        report("Expected a constant expression", textLocation)
    }

    fun reportUnreachableCode(textLocation: TextLocation) {
        report("Unreachable code detected", textLocation, diagnosticType = DiagnosticType.Warning)
    }

    fun reportImportMustBeFirstStatement(member: ImportStatementSyntax) {
        report("Import statement must be first statement in file", member.location)
    }

    fun reportImportedFileNotFound(filePath: String, textLocation: TextLocation) {
        report("Imported file '$filePath' not found", textLocation)
    }

    fun reportCircularDependency(location: TextLocation, cycle: List<ImportGraphNode>) {
        val cycleString = cycle.joinToString(" -> ") { it.fileName }
        report("Circular dependency detected: $cycleString", location)
    }


}