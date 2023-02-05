package lang.proteus.symbols

 class TypeSymbol(val name: String, moduleReferenceSymbol: ModuleReferenceSymbol) : Symbol(
    name,
    moduleReferenceSymbol,
) {
    override fun toString(): String {
        return name
    }

    companion object {
        public val Error: TypeSymbol = TypeSymbol("Error", ModuleReferenceSymbol(listOf("lang", "proteus")))
        public val Int: TypeSymbol = TypeSymbol("Int", ModuleReferenceSymbol(listOf("lang", "proteus")))
        public val String = TypeSymbol("String", ModuleReferenceSymbol(listOf("lang", "proteus")))
        public val Boolean = TypeSymbol("Boolean", ModuleReferenceSymbol(listOf("lang", "proteus")))
        public val Unit = TypeSymbol("Unit", ModuleReferenceSymbol(listOf("lang", "proteus")))
        public val Type = TypeSymbol("Type", ModuleReferenceSymbol(listOf("lang", "proteus")))
        public val Any = TypeSymbol("Any", ModuleReferenceSymbol(listOf("lang", "proteus")))
    }

     fun isAssignableTo(other: TypeSymbol): Boolean {
         return this == other
     }

     override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as TypeSymbol

            if (name != other.name) return false
            if (moduleReferenceSymbol != other.moduleReferenceSymbol) return false

            return true
     }
}