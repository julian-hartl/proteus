import lang.proteus.api.ProteusCompiler

fun main(args: Array<String>) {
    val verbose = args.contains("-v")
    val variables: Map<String, Any> = mapOf(
        "x" to 1
    )
    val compiler = ProteusCompiler(variables)
    while (true) {
        val line =
            run {
                print("> ")
                readlnOrNull()
            } ?: continue
        if (line == "quit") {
            break
        }
        compiler.compile(line, verbose = verbose)
    }

}
