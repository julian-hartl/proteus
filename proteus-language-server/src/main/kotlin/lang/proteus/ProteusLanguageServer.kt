package lang.proteus

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.services.*
import java.util.concurrent.CompletableFuture

class ProteusLanguageServer() : LanguageServer, LanguageClientAware {

    private lateinit var client: LanguageClient

    override fun initialize(params: InitializeParams?): CompletableFuture<InitializeResult> {
        val capabilities = ServerCapabilities()
        capabilities.setTextDocumentSync(TextDocumentSyncKind.Full)
        capabilities.setCodeActionProvider(false)
        capabilities.completionProvider =
            CompletionOptions(false, null) // here you can specify when the auto completion should be triggered
        return CompletableFuture.completedFuture(InitializeResult())
    }

    override fun shutdown(): CompletableFuture<Any> {
        return CompletableFuture.completedFuture(null)
    }

    override fun exit() {

    }

    override fun getTextDocumentService(): TextDocumentService {
        return ProteusTextDocumentService(client)
    }

    override fun getWorkspaceService(): WorkspaceService {
        return ProteusWorkspaceService()
    }

    override fun connect(client: LanguageClient?) {
        this.client = client!!
    }
}