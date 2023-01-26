package lang.proteus.emit

import lang.proteus.binding.BoundLabel
import lang.proteus.symbols.VariableSymbol
import org.objectweb.asm.Label

internal data class StackFrame(
    private val localVariables: MutableMap<String, Int> = mutableMapOf(),
    private val labels: MutableMap<String, Label> = mutableMapOf(),
    val start: Label = Label(),
    val end: Label = Label(),
) {
    var localVariablePointer = 0

    fun getLocalVariableIndex(variable: VariableSymbol): Int {
        return localVariables[variable.qualifiedName] ?: -1
    }

    fun defineLocalVariable(variable: VariableSymbol): Int {
        val index = localVariablePointer
        localVariablePointer++
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

    fun defineLabel(label: BoundLabel): Label {
        val labelName = label.name
        val labelValue = Label()
        labels[labelName] = labelValue
        return labelValue
    }

    fun lookupLabel(label: BoundLabel): Label? {
        return labels[label.name]
    }
}
