package lang.proteus.syntax.parser

import lang.proteus.diagnostics.Diagnostic
import lang.proteus.diagnostics.Diagnostics

class ErrorAsserter(private val diagnosticsWrapper: Diagnostics) {

    private val diagnostics: List<Diagnostic>
        get() = diagnosticsWrapper.errors

    companion object {
        fun fromDiagnostics(diagnostics: Diagnostics): ErrorAsserter {
            return ErrorAsserter(diagnostics)
        }
    }

    fun assertNoErrors() {
        if (diagnosticsWrapper.hasErrors()) {
            throw AssertionError("Expected no errors, but found ${diagnostics.size} errors: $diagnostics")
        }
    }

    fun assertErrorFromTo(from: Int, to: Int) {
        val error = diagnostics.filter {
            it.span.start == from && it.span.end == to
        }
        if (error.isEmpty()) {
            throw AssertionError("Expected error from $from to $to, but found none. All errors: $diagnostics. Which have ranges: ${diagnostics.map { it.span }}")
        }
    }


}