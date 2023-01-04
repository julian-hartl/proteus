package lang.proteus.diagnostics

import lang.proteus.printing.ConsolePrinter
import lang.proteus.printing.PrinterColor

class MutableDiagnostics private constructor(private val mutableDiagnostics: MutableList<Diagnostic>) : Diagnostics {

    constructor() : this(mutableListOf())

    private val printer: ConsolePrinter = ConsolePrinter()

    init {
        printer.setColor(PrinterColor.RED)
    }

    override fun size() = mutableDiagnostics.size

    fun add(message: String, literal: String, position: Int) {
        mutableDiagnostics.add(Diagnostic(message, literal, position))
    }

    override fun print() {
        mutableDiagnostics.forEach {
            printer.println(it.toString())
        }
    }

    override val diagnostics: List<Diagnostic>
        get() = mutableDiagnostics.toList()

}