package lang.proteus

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.TextDocumentService

class ProteusTextDocumentService(private val client: LanguageClient) : TextDocumentService {
    override fun didOpen(params: DidOpenTextDocumentParams?) {

    }

    override fun didChange(params: DidChangeTextDocumentParams?) {
        if (params != null) {
            val contentChanges = params.contentChanges
            val diagnostics = contentChanges.map {
                val diagnostic = Diagnostic()
                diagnostic.message = "Hello World"
                diagnostic.range = it.range
                diagnostic
            }

            client.publishDiagnostics(PublishDiagnosticsParams(params.textDocument.uri, diagnostics))
        }
    }

    override fun didClose(params: DidCloseTextDocumentParams?) {
    }

    override fun didSave(params: DidSaveTextDocumentParams?) {
    }
}