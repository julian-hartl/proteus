import lang.proteus.ProteusLanguageServer
import org.eclipse.lsp4j.launch.LSPLauncher
import java.net.Socket

fun main(args: Array<String>) {
    val socket = Socket("localhost", 5007)
    socket.use {
        val languageServer = ProteusLanguageServer()
        val rpcServer =
            LSPLauncher.createServerLauncher(languageServer, socket.getInputStream(), socket.getOutputStream())
        val client = rpcServer.remoteProxy
        languageServer.connect(client)
        rpcServer.startListening()
    }
}