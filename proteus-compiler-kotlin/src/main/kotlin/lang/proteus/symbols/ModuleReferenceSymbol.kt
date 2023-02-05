package lang.proteus.symbols

import lang.proteus.metatdata.Metadata

 data class ModuleReferenceSymbol(
    val parts: List<String>,
) {
    /*
    * Resolves this module reference to an absolute file path.
    *
    * It does so, relative to the file path of the module that contains this reference.
    *
    * This file path points to the exact location of the file where this module was defined.
    *
    * For example:
    * ```
    * // File: /home/user/project/src/main/proteus/MyModule.proteus
    * import my_module::my_function;
    * ```
    *
    * In this example, the module reference `my_module` is resolved to the absolute file path:
    * `/home/user/project/src/main/proteus/my_module.psl`
     */
    fun resolve(relativeTo: ModuleReferenceSymbol): String {
        val parts = parts.toMutableList()
        val lastPart = parts.removeLast()
        val path = parts.joinToString("/")
        val relativeDir = relativeTo.directory
        return "$relativeDir/$path/$lastPart.${Metadata.PROTEUS_FILE_EXTENSION}"
    }

    val directory get() = parts.dropLast(1).joinToString("/")
    val name get() = parts.last()
}

