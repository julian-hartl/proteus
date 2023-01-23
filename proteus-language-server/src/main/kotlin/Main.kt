import lang.proteus.ProteusLanguageServer
import org.eclipse.lsp4j.launch.LSPLauncher
import java.net.ServerSocket

fun main(args: Array<String>) {
    val serverSocket = ServerSocket(63456)
    val socket = serverSocket.accept()
    socket.use {
        val languageServer = ProteusLanguageServer()
        val rpcServer =
            LSPLauncher.createServerLauncher(languageServer, it.getInputStream(), it.getOutputStream())
        val client = rpcServer.remoteProxy
        languageServer.connect(client)
        rpcServer.startListening()
    }
}