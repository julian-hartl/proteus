package lang.proteus.diagnostics

class MutableDiagnostics private constructor(private var mutableDiagnostics: MutableList<Diagnostic>) : Diagnostics {

    constructor() : this(mutableListOf())


    fun add(diagnostic: Diagnostic) {
        mutableDiagnostics.add(diagnostic)
    }


    override val diagnostics: List<Diagnostic>
        get() = mutableDiagnostics.toList()

    override fun hasErrors(): Boolean {
        return mutableDiagnostics.size > 0
    }

    override fun concat(other: Diagnostics) {
        val newDiagnostics = mutableListOf<Diagnostic>()
        newDiagnostics.addAll(mutableDiagnostics)
        newDiagnostics.addAll(other.diagnostics)
        mutableDiagnostics = newDiagnostics
    }

    override fun toString(): String {
        return mutableDiagnostics.joinToString("\n")
    }

}