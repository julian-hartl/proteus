package lang.proteus.api.input

import kotlin.properties.ObservableProperty
import kotlin.reflect.KProperty
import kotlin.system.exitProcess


internal abstract class Repl {

    fun run() {
        while (true) {
            val submission = editSubmission()
            if (submission == null) {
                exitProcess(0)
            }
            evaluateSubmission(submission)

        }
    }

    private class SubmissionView(private val initialLines: List<String>) {

        private val top: Int = 0
        private var renderedLineCount: Int = 0
        private var _currentLineIndex: Int = 0
        private var _currentCharacter: Int = 0
        val document = ObservableDocument(initialLines)

        val line: String
            get() = document.value[currentLineIndex]

        private val submissionViewWindow = SubmissionViewWindow()


        val window: SubmissionViewWindow
            get() = submissionViewWindow

        inner class ObservableDocument(initialLines: List<String>) :
            ObservableProperty<List<String>>(initialLines) {
            override fun afterChange(property: KProperty<*>, oldValue: List<String>, newValue: List<String>) {
                render()
            }

            var value: List<String>
                get() = super.getValue(this, this::value)
                set(value) = super.setValue(this, this::value, value)

            fun insertCharacter(character: Char, lineIndex: Int, characterIndex: Int) {
                val line = this.value[lineIndex]
                val newLine = line.substring(0, characterIndex) + character + line.substring(characterIndex)
                val newLines = this.value.toMutableList()
                newLines[lineIndex] = newLine
                value = newLines
            }

            fun insertNewLine() {
                val line = this.value[currentLineIndex]
                val newLine = line.substring(0, currentCharacter)
                val newLines = this.value.toMutableList()
                newLines[currentLineIndex] = newLine
                newLines.add(currentLineIndex + 1, line.substring(currentCharacter))
                value = newLines
            }
        }


        private fun render() {
            submissionViewWindow.setCursorPosition(0, top)
            submissionViewWindow.setCursorVisible(false)
            for ((index, line) in document.value.withIndex()) {
                submissionViewWindow.setColor(PrinterColor.GREEN)

                if (index == 0) submissionViewWindow.write("> ")
                else {
                    submissionViewWindow.write(" ")
                }

                submissionViewWindow.resetColor()

                submissionViewWindow.writeLine(line)
            }

            val numberOfBlankLines = renderedLineCount - document.value.size
            if (numberOfBlankLines > 0) {
                val blankLine = " ".repeat(submissionViewWindow.width)
                while (numberOfBlankLines > 0) {
                    submissionViewWindow.writeLine(blankLine)
                }
            }

            renderedLineCount = top + currentCharacter
            submissionViewWindow.setCursorVisible(true)
            updateCursorPosition()

        }

        var currentLineIndex
            set(value) {
                if (_currentLineIndex != value) {
                    _currentLineIndex = value
                    updateCursorPosition()
                }
            }
            get() = _currentLineIndex

        var currentCharacter
            set(value) {
                if (_currentCharacter != value) {
                    _currentCharacter = value
                    updateCursorPosition()
                }
            }
            get() = _currentCharacter

        private fun updateCursorPosition() {
            val cursorPosition = submissionViewWindow.cursorPosition
            val top = cursorPosition.y + currentLineIndex
            val left = cursorPosition.x + 2 + currentCharacter
            submissionViewWindow.setCursorPosition(left, top)
        }
    }

    private enum class PrinterColor(val ansiCode: Byte) {
        BLACK(30),
        RED(31),
        GREEN(32),
        YELLOW(33),
        BLUE(34),
        MAGENTA(35),
        CYAN(36),
        WHITE(38);
    }

    private data class CursorPosition(val x: Int, val y: Int)
    private data class WindowSize(val width: Int, val height: Int)

    private class SubmissionViewWindow {

        private val writer = System.out.writer()

        private val reader = System.`in`.bufferedReader()

        private var color = PrinterColor.WHITE

        private var _width: Int? = null
        private var _height: Int? = null

        private var _cursorPosition: CursorPosition? = null

        fun setCursorVisible(visible: Boolean) {
            if (visible) {
                writer.write("\u001b[?25h")
            } else {
                writer.write("\u001b[?25l")
            }
            writer.flush()
        }

        val cursorPosition: CursorPosition
            get() {
                if (_cursorPosition == null) {
                    _cursorPosition = initCursorPosition()
                }
                return _cursorPosition!!
            }

        val width: Int
            get() {
                if (_width != null) {
                    return _width!!
                }
                val size = getWindowSize()
                _width = size.width
                _height = size.height
                return _width!!
            }

        val height: Int
            get() {
                if (_height != null) {
                    return _height!!
                }
                val size = getWindowSize()
                _width = size.width
                _height = size.height
                return _height!!
            }

        private fun getWindowSize(): WindowSize {
            write("\u001b[s")             // save cursor position
            setCursorPosition(5000, 5000)  // move to col 5000 row 5000
            write("\u001b[6n")            // request cursor position
            write("\u001b[u")             // restore cursor position
            val cursorPosition = initCursorPosition()
            return WindowSize(cursorPosition.x, cursorPosition.y)
        }

        private fun initCursorPosition(): CursorPosition {
            write("\u001b[6n")
            val response = reader.readLine()
            val parts = response.split(';')
            val x = parts[0].toInt()
            val y = parts[1].toInt()
            return CursorPosition(x, y)
        }

        fun setCursorPosition(row: Int, column: Int) {
            writer.write(String.format("\u001b[%d;%dH", row, column))
        }

        fun writeLine(text: String) {
            writer.write(buildColoredString(text) + System.lineSeparator())
        }

        fun write(text: String) {
            writer.write(buildColoredString(text))
        }

        fun setColor(color: PrinterColor) {
            this.color = color
        }

        fun resetColor() {
            this.color = PrinterColor.WHITE
        }

        private fun buildColoredString(text: String): String {
            return "\u001B[${color.ansiCode}m$text\u001B[0m"
        }

        fun readKey(display: Boolean = true): Char {
            if (display) {
                writer.write("\u001b[?25h")
            } else {
                writer.write("\u001b[?25l")
            }
            writer.flush()
            return reader.read().toChar()
        }


    }

    private fun editSubmission(): String? {
        val submissionView = SubmissionView(mutableListOf<String>())
        while (true) {
            val key = submissionView.window.readKey(display = false)
            handleKey(key, submissionView)
        }
    }

    private fun handleKey(key: Char, submissionView: SubmissionView) {
        when (key) {
            '\u001b' -> {
                val nextKey = submissionView.window.readKey(display = false)
                if (nextKey == '[') {
                    val direction = submissionView.window.readKey(display = false)
                    handleArrowKey(direction, submissionView)
                }
            }

            '\u007f' -> {
                handleBackspace(submissionView)
            }

            '\u000d' -> {
                handleEnter(submissionView)
            }

            else -> {
                handleCharacter(key, submissionView)
            }
        }
    }

    private fun handleBackspace(submissionView: SubmissionView) {

    }

    private fun handleEnter(submissionView: SubmissionView) {
        submissionView.document.insertNewLine()
    }

    private fun handleArrowKey(direction: Char, submissionView: SubmissionView) {
        when (direction) {
            'A' -> {
                if (submissionView.currentLineIndex > 0) {
                    submissionView.currentLineIndex--
                }
            }

            'B' -> {
                if (submissionView.currentLineIndex < submissionView.document.value.size - 1) {
                    submissionView.currentLineIndex++
                }
            }

            'C' -> {
                if (submissionView.currentCharacter < submissionView.line.length) {
                    submissionView.currentCharacter++
                }
            }

            'D' -> {
                if (submissionView.currentCharacter > 0) {
                    submissionView.currentCharacter--
                }
            }
        }
    }

    private fun handleCharacter(key: Char, submissionView: SubmissionView) {
        val lineIndex = submissionView.currentLineIndex
        val start = submissionView.currentCharacter
        submissionView.document.insertCharacter(key, lineIndex, start)
    }

    private fun editSubmissionOld(): String? {
        print("> ")
        val textBuilder = StringBuilder()
        while (true) {
            val line = readlnOrNull()
            if (textBuilder.isEmpty()) {

                if (line.isNullOrEmpty()) {
                    return null
                }
                if (line.startsWith(":")) {
                    evaluateCommand(line.substring(1))
                    continue
                }
            }
            textBuilder.appendLine(line)
            val text = textBuilder.toString()
            if (isCompleteSubmission(text)) {
                return text
            }

            print("  ")
        }

    }

    private fun evaluateCommand(command: String) {
        if (command == "q") {
            exitProcess(0)
        }
        println("Unknown command: $command")
    }

    protected abstract fun evaluateSubmission(text: String)

    protected abstract fun isCompleteSubmission(text: String): Boolean
}