package lang.proteus.binding

class VariableContainer(private val variables: MutableMap<VariableSymbol, Any> = mutableMapOf()) {

    companion object {
        fun fromUntypedMap(variables: Map<String, Any>): VariableContainer {
            val typedVariables = mutableMapOf<VariableSymbol, Any>()
            for ((key, value) in variables) {
                typedVariables[VariableSymbol(key, ProteusType.fromValueOrObject(value))] = value
            }
            return VariableContainer(typedVariables)
        }
    }

    private fun declareVariable(symbol: VariableSymbol, value: Any) {
        variables[symbol] = value
    }

    fun assignVariable(symbol: VariableSymbol, value: Any) {
        if(symbol !in variables) {
            declareVariable(symbol, value)
        } else {
            variables[symbol] = value
        }
    }

    fun getVariableValue(name: String): Any? {
        return variables.entries.firstOrNull { it.key.name == name }?.value
    }

    fun getVariableValue(symbol: VariableSymbol): Any? {
        return variables[symbol]
    }

    fun canAssignTo(symbol: VariableSymbol, type: ProteusType): Boolean {
        return type.isAssignableTo(symbol.type)
    }

    fun getVariableSymbol(name: String): VariableSymbol? {
        return variables.entries.firstOrNull { it.key.name == name }?.key
    }

    val untypedVariables: Map<String, Any>
        get() {
            val untypedVariables = mutableMapOf<String, Any>()
            for ((key, value) in variables) {
                untypedVariables[key.name] = value
            }
            return untypedVariables
        }
}