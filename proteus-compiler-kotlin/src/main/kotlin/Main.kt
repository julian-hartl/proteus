import lang.proteus.api.ProteusCompiler

fun main(args: Array<String>) {
    val verbose = args.contains("-v")
    val variables: Map<String, Any> = mapOf(
        "x" to 1
    )
    val compiler = ProteusCompiler(variables)
    val textBuilder = StringBuilder()
    while (true) {
        val line =
            run {
                print("> ")
                readlnOrNull()
            } ?: continue
        if (textBuilder.isEmpty()) {

            if (line == "quit") {
                break
            }
            if (line.isBlank()) {
                break
            }
        }
        textBuilder.appendLine(line)
        println(textBuilder.toString())
        try {
            compiler.compile(textBuilder.toString(), verbose = verbose)
        } catch (e: Exception) {
            e.printStackTrace()
            println()
        }
    }

}
