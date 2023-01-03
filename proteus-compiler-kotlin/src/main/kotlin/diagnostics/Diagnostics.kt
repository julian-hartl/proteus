package diagnostics

import printing.ConsolePrinter
import printing.PrinterColor

class Diagnostics private constructor(private val diagnostics: MutableList<Diagnostic>) {

    constructor() : this(mutableListOf())

    private val printer: ConsolePrinter = ConsolePrinter()

    init {
        printer.setColor(PrinterColor.RED)
    }

    fun size() = diagnostics.size

    fun add(message: String, literal: String, position: Int) {
        diagnostics.add(Diagnostic(message, literal, position))
    }

    fun print() {
        diagnostics.forEach {
            printer.println(it.toString())
        }
    }

}