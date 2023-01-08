package lang.proteus.api.input

import lang.proteus.printing.ConsolePrinter

class ConsoleInputReader : ProteusSourceTextInputReader() {

    private val consolePrinter = ConsolePrinter()

    override fun read(): String? {
        val text =
            run {
                val textBuilder = StringBuilder()
                consolePrinter.print("> ")
                var line = readlnOrNull()
                while (line != null && line != "") {
                    textBuilder.appendLine(line)
                    consolePrinter.print("| ")
                    line = readlnOrNull()
                }
                textBuilder.toString()
            }

        if (text == "quit") {
            return null
        }
        if (text.isBlank()) {
            return null
        }
        return text
    }

}