package lang.proteus.emit

import lang.proteus.binding.BoundLabel
import lang.proteus.symbols.FunctionSymbol
import lang.proteus.symbols.VariableSymbol
import org.objectweb.asm.Label

internal data class StackFrame(
    val functionSymbol: FunctionSymbol,
) {
    private val localVariables: MutableMap<String, Int> = mutableMapOf()
    private val labels: MutableMap<String, Label> = mutableMapOf()
    val startLabel: Label = Label()
    val endLabel: Label = Label()
    private var _localVariablePointer = 0

    public val localVariablePointer: Int
        get() = _localVariablePointer

    fun getLocalVariableIndex(variable: VariableSymbol): Int {
        return localVariables[variable.qualifiedName]
            ?: throw IllegalStateException("Variable ${variable.qualifiedName} not found in local variables")
    }

    fun lookupOrDefineLocalVariable(variable: VariableSymbol): Int {
        val existingIndex = localVariables[variable.qualifiedName]
        if (existingIndex != null) {
            return existingIndex
        }
        return defineLocalVariable(variable)
    }

    fun defineLocalVariable(variable: VariableSymbol): Int {
        val index = _localVariablePointer
        _localVariablePointer++
        localVariables[variable.qualifiedName] = index
        return index
    }

    fun lookupOrDefineLabel(label: BoundLabel): Label {
        val existingLabel = lookupLabel(label)
        if (existingLabel != null) {
            return existingLabel
        }
        return defineLabel(label)
    }

    private fun defineLabel(label: BoundLabel): Label {
        val labelName = label.name
        val labelValue = Label()
        labels[labelName] = labelValue
        return labelValue
    }

    fun lookupLabel(label: BoundLabel): Label? {
        return labels[label.name]
    }
}
