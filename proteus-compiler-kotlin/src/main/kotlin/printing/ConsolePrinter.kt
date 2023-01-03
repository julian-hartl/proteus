package printing


enum class PrinterColor(val ansiCode: Byte) {
    RED(31),
    GREEN(32),
    YELLOW(33),
    BLUE(34),
    MAGENTA(35),
    CYAN(36),
    WHITE(37);
}

class ConsolePrinter {

    private var color: PrinterColor = PrinterColor.WHITE

    fun println(text: String) {
        kotlin.io.println(buildColoredString(text))
    }

    private fun buildColoredString(text: String): String {
        return "\u001B[${color.ansiCode}m$text\u001B[0m"
    }

    fun setColor(color: PrinterColor) {
        this.color = color
    }
}