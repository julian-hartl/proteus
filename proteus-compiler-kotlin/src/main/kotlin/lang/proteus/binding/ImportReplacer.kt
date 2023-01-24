package lang.proteus.binding

import lang.proteus.api.Compilation
import lang.proteus.diagnostics.MutableDiagnostics
import lang.proteus.syntax.parser.ImportStatementSyntax
import lang.proteus.syntax.parser.SyntaxTree

internal data class ReplaceImportResult(
    val statements: List<BoundStatement>,
    val diagnostics: MutableDiagnostics,
    val globalScope: BoundGlobalScope,
)

internal class ImportReplacer private constructor(
    private val importStatement: ImportStatementSyntax,
    private val diagnostics: MutableDiagnostics = MutableDiagnostics(),
) {
    private lateinit var globalScope: BoundGlobalScope

    companion object {

        fun replaceImport(importStatement: ImportStatementSyntax): ReplaceImportResult {
            val importReplacer = ImportReplacer(importStatement)
            val statements = importReplacer.replaceImport()
            val diagnostics = importReplacer.diagnostics
            return ReplaceImportResult(statements, diagnostics, importReplacer.globalScope)
        }

    }

    private fun replaceImport(): List<BoundStatement> {
        val importSyntaxTree = SyntaxTree.load(importStatement.resolvedFilePath)
        val importCompilation = Compilation(importSyntaxTree)
        val importGlobalScope = importCompilation.globalScope
        val importDiagnostics = importGlobalScope.diagnostics
        globalScope = importGlobalScope
        if (importDiagnostics.hasErrors() || importSyntaxTree.hasErrors()) {
            diagnostics.concat(importSyntaxTree.diagnostics)
            diagnostics.concat(importDiagnostics)
            return emptyList()
        }
        val importProgram = Binder.bindProgram(importGlobalScope)
        globalScope = importProgram.globalScope
        return importProgram.statement.statements
    }


}