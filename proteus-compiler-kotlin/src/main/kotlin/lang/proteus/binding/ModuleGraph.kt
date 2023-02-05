package lang.proteus.binding

import lang.proteus.diagnostics.DiagnosticsBag
import lang.proteus.parser.CompilationUnit
import lang.proteus.parser.Parser
import lang.proteus.symbols.ImportSymbol
import lang.proteus.symbols.Symbol


internal data class ModuleGraphEdge(
    val from: Module,
    val to: Module,
)

internal class ModuleGraph {

    val diagnosticsBag = DiagnosticsBag()

    companion object {

        fun create(module: Module): ModuleGraph {
            val graph = ModuleGraph()
            return graph.create(module)
        }

    }

    private val treeCache = mutableMapOf<String, CompilationUnit>()

    private fun create(module: Module): ModuleGraph {
        addModule(module)
        val symbolGatherer = SymbolGatherer(module)
        val symbols = symbolGatherer.gatherSymbols()
        val imports = symbols.filterIsInstance<ImportSymbol>()
        for (import in imports) {
            val importPath = import.module.resolve(module.moduleReference)
            val compilationUnit = treeCache.getOrPut(importPath) {
                Parser.load(importPath)
            }
            val importedModule = Module(import.module, compilationUnit)
            val hasAddedEdge = addEdge(module, importedModule)
            if (hasAddedEdge)
                create(importedModule)
        }
        return this
    }


    private val modules = mutableSetOf<Module>()
    private val edges = mutableSetOf<ModuleGraphEdge>()

    private fun addModule(module: Module): Boolean {
        return modules.add(module)
    }

    private fun addEdge(from: Module, to: Module): Boolean {
        addModule(from)
        addModule(to)
        val edge = ModuleGraphEdge(from, to)
        return edges.add(edge)
    }

    fun getModules(): Set<Module> {
        return modules
    }

    private val cachedExportedSymbols = mutableMapOf<Module, List<Symbol>>()

    fun gatherImportedSymbols(module: Module): Set<Symbol> {
        val importedSymbols = mutableSetOf<Symbol>()
        val outgoingEdges = getOutgoingEdges(module)
        for (edge in outgoingEdges) {
            val importedTree = edge.to
            val exportedSymbols = gatherExportedSymbols(importedTree)
            importedSymbols.addAll(exportedSymbols)
        }
        for (importedSymbol1 in importedSymbols) {
            for (importedSymbol2 in importedSymbols) {
                if (importedSymbol1 === importedSymbol2) continue
                if (importedSymbol1.conflictsWith(importedSymbol2)) {
//                    diagnosticsBag.reportConflictingImport(
//                        importedSymbol1,
//                        tree1,
//                        importedSymbol2,
//                        tree2,
//                        importStatementSyntax
//                    )
                    //todo: report error
                }
            }
        }
        return importedSymbols
    }

    fun gatherExportedSymbols(module: Module): Set<Symbol> {
        val symbolGatherer = SymbolGatherer(module)
        val symbols = symbolGatherer.gatherSymbols()
        cachedExportedSymbols[module] = symbols
        return symbols.toSet()
    }

    private fun getOutgoingEdges(module: Module): List<ModuleGraphEdge> {
        return edges.filter { it.from == module }
    }


    fun findCycles(): List<List<Module>> {
        val cycles = mutableListOf<List<Module>>()
        val visited = mutableSetOf<Module>()
        val stack = mutableListOf<Module>()
        for (node in modules) {
            if (node in visited) {
                continue
            }
            findCycles(node, visited, stack, cycles)
        }
        return cycles
    }

    private fun findCycles(
        node: Module,
        visited: MutableSet<Module>,
        stack: MutableList<Module>,
        cycles: MutableList<List<Module>>,
    ) {
        visited.add(node)
        stack.add(node)
        for (edge in edges) {
            if (edge.from != node) {
                continue
            }
            val toNode = edge.to
            if (toNode in stack) {
                val cycle = mutableListOf<Module>()
                var i = stack.size - 1
                while (i >= 0) {
                    val stackNode = stack[i]
                    cycle.add(stackNode)
                    if (stackNode == toNode) {
                        break
                    }
                    i--
                }
                cycles.add(cycle)
            } else if (toNode !in visited) {
                findCycles(toNode, visited, stack, cycles)
            }
        }
        stack.removeAt(stack.size - 1)
    }
}