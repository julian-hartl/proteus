package lang.proteus.binding

import lang.proteus.parser.CompilationUnit
import lang.proteus.symbols.ModuleReferenceSymbol

internal data class Module(val moduleReference: ModuleReferenceSymbol, val compilationUnit: CompilationUnit)