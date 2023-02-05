package lang.proteus.symbols

import lang.proteus.binding.Module

internal data class ImportSymbol(val module: ModuleReferenceSymbol): Symbol(
    uniqueIdentifier = module.parts.joinToString("_"),
    simpleName = module.parts.last(),
)