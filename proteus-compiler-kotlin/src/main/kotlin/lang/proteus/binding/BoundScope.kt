package lang.proteus.binding

internal class BoundScope internal constructor(val parent: BoundScope?) {
    private val variables: MutableMap<String, VariableSymbol> = mutableMapOf()

    fun getDeclaredVariables(): List<VariableSymbol> = variables.values.toList()

    fun tryDeclare(variable: VariableSymbol): Boolean {
        if (variables.containsKey(variable.name)) {
            return false
        }

        variables[variable.name] = variable
        return true
    }

    fun tryLookup(variable: String): VariableSymbol? {
        if (variables.containsKey(variable)) {
            return variables[variable]!!
        }
        if (parent == null) return null
        return parent.tryLookup(variable)

    }

}