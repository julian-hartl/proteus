package lang.proteus.diagnostics

import lang.proteus.binding.ImportGraphNode
import lang.proteus.symbols.FunctionSymbol
import lang.proteus.symbols.Symbol
import lang.proteus.symbols.TypeSymbol
import lang.proteus.symbols.VariableSymbol
import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Token
import lang.proteus.syntax.parser.FunctionDeclarationSyntax
import lang.proteus.syntax.parser.ImportStatementSyntax
import lang.proteus.syntax.parser.SyntaxTree

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

    private fun report(
        message: String,
        location: TextLocation,
        diagnosticType: DiagnosticType = DiagnosticType.Error,
        hint: String? = null,
    ) {
        val errorMessage = if (hint != null) "$message --- Hint: $hint" else message
        mutableDiagnostics.add(Diagnostic(errorMessage, location, diagnosticType))
    }

    fun concat(other: DiagnosticsBag) {
        mutableDiagnostics.concat(other.mutableDiagnostics)
    }

    fun reportCannotConvert(textLocation: TextLocation, expectedType: TypeSymbol, actualType: TypeSymbol) {
        if (actualType !is TypeSymbol.Error) {
            report("Cannot convert type '${actualType}' to '${expectedType.toString()}'", textLocation)
        }
    }

    fun reportVariableAlreadyDeclared(textLocation: TextLocation, variableName: String) {
        report("Variable '$variableName' already declared", textLocation)
    }

    fun reportFinalVariableCannotBeReassigned(textLocation: TextLocation, variable: VariableSymbol) {
        val variableDeclarationLiteral = variable.declarationLiteral
        report(
            "Readonly variables cannot be reassigned",
            textLocation,
            hint = "Variable '${variable.simpleName}' is declared as '$variableDeclarationLiteral'"
        )
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
        for (diagnostic in diagnostics.errors) {
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
        val cycleString = cycle.joinToString(" -> ") { it.tree.sourceText.absolutePath }
        report("Circular dependency detected: $cycleString", location)
    }

    fun invalidMainFunctionReturnType(mainFunction: FunctionSymbol, unit: TypeSymbol.Unit) {
        report(
            "Invalid return type. Expected '${unit}', got '${mainFunction.returnType}'",
            mainFunction.declaration.location
        )
    }

    fun reportInvalidImport(importStatement: ImportStatementSyntax) {
        report(
            "Invalid import",
            importStatement.location,
            hint = "Import should be a relative import or a library import"
        )
    }

    fun reportDuplicateModifier(current: SyntaxToken<*>) {
        report("Duplicate modifier '${current.literal}'", current.location)
    }

    fun reportExternalFunctionNotFound(declaration: FunctionDeclarationSyntax, location: TextLocation) {
        report(
            "External function ${declaration.identifier.literal} not found",
            location,
            hint = "Install the required library."
        )
    }

    fun reportConflictingImport(
        importedSymbol1: Symbol,
        tree1: SyntaxTree,
        importedSymbol2: Symbol,
        tree2: SyntaxTree,
        conflictingImportStatement: ImportStatementSyntax,
    ) {
        report(
            "Conflicting import. Both '${importedSymbol1.simpleName}' and '${importedSymbol2.simpleName}' are imported from different files.",
            conflictingImportStatement.location,
            hint = "Imported from '${tree1.sourceText.absolutePath}' and '${tree2.sourceText.absolutePath}'"
        )
    }

    fun reportCannotCallMain(location: TextLocation) {
        report("Cannot call main function", location, hint = "The main function is called automatically.")
    }

    fun reportUnusedExpression(location: TextLocation) {
        report("Expression is unused", location, diagnosticType = DiagnosticType.Warning)
    }

    fun reportStructAlreadyDeclared(location: TextLocation, name: String) {
        report("Struct '$name' already declared", location)
    }

    fun reportStructMemberAlreadyDeclared(location: TextLocation, name: String) {
        report("Struct member '$name' already declared", location)
    }

    fun reportUndefinedStruct(location: TextLocation, structName: String) {
        report("Undefined struct '$structName'", location)
    }

    fun reportUndefinedStructMember(location: TextLocation, memberName: String, name: String) {
        report("Undefined struct member '$memberName' in struct '$name'", location)
    }

    fun reportStructMemberAlreadyInitialized(location: TextLocation, memberName: String, name: String) {
        report("Struct member '$memberName' in struct '$name' already initialized", location)
    }

    fun reportStructMemberNotInitialized(location: TextLocation, name: String, structName: String) {
        report("Struct member '$name' in struct '$structName' not initialized", location)
    }

    fun reportUndefinedMember(location: TextLocation, memberName: String, type: TypeSymbol) {
        report("Undefined member '$memberName' in type '${type}'", location)
    }

    fun reportInvalidName(location: TextLocation) {
        report("Invalid name", location)
    }

    fun reportCannotReference(location: TextLocation) {
        report("Cannot reference this symbol", location)
    }

    fun reportCannotDereference(location: TextLocation, type: TypeSymbol) {
        report("Cannot dereference type '${type}'", location)
    }


}