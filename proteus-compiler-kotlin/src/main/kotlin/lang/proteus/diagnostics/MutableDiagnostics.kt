package lang.proteus.diagnostics

import lang.proteus.diagnostics.TextSpan
import lang.proteus.printing.ConsolePrinter
import lang.proteus.printing.PrinterColor

class MutableDiagnostics private constructor(private var mutableDiagnostics: MutableList<Diagnostic>) : Diagnostics {

    constructor() : this(mutableListOf())

    private val printer: ConsolePrinter = ConsolePrinter()

    init {
        printer.setColor(PrinterColor.RED)
    }


    fun add(diagnostic: Diagnostic) {
        mutableDiagnostics.add(diagnostic)
    }


    override fun print() {
        mutableDiagnostics.forEach {
            printer.println(it.toString())
        }
    }

    override val diagnostics: List<Diagnostic>
        get() = mutableDiagnostics.toList()

    override fun hasErrors(): Boolean {
        return mutableDiagnostics.size > 0
    }

    override fun concat(other: Diagnostics){
        val newDiagnostics = mutableListOf<Diagnostic>()
        newDiagnostics.addAll(mutableDiagnostics)
        newDiagnostics.addAll(other.diagnostics)
        mutableDiagnostics = newDiagnostics
    }

    override fun toString(): String {
        return mutableDiagnostics.joinToString("\n")
    }

}