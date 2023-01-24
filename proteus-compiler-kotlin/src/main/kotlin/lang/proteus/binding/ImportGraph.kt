package lang.proteus.binding

import lang.proteus.diagnostics.DiagnosticsBag
import lang.proteus.symbols.Symbol
import lang.proteus.syntax.parser.FunctionDeclarationSyntax
import lang.proteus.syntax.parser.GlobalVariableDeclarationSyntax
import lang.proteus.syntax.parser.ImportStatementSyntax
import lang.proteus.syntax.parser.SyntaxTree

internal data class ImportGraphNode(
    val tree: SyntaxTree,
)

internal data class ImportGraphEdge(
    val from: ImportGraphNode,
    val to: ImportGraphNode,
    val importStatementSyntax: ImportStatementSyntax,
)

internal class ImportGraph {

    val diagnosticsBag = DiagnosticsBag()

    companion object {

        fun create(mainTree: SyntaxTree): ImportGraph {
            val graph = ImportGraph()
            return create(graph, mainTree)
        }

        private val syntaxTreeCache = mutableMapOf<String, SyntaxTree>()

        private fun create(graph: ImportGraph, tree: SyntaxTree): ImportGraph {
            if (tree.hasErrors())
                return graph
            graph.addNode(tree)
            val members = tree.root.members
            val importStatements = members.filterIsInstance<ImportStatementSyntax>()
            for (importStatement in importStatements) {
                val importPath = importStatement.resolvedFilePath
                val importedTree = syntaxTreeCache[importPath] ?: SyntaxTree.load(importPath)
                syntaxTreeCache[importPath] = importedTree
                val hasAddedEdge = graph.addEdge(tree, importedTree, importStatement)
                if (hasAddedEdge)
                    create(graph, importedTree)
            }
            return graph
        }
    }


    private val nodes = mutableMapOf<SyntaxTree, ImportGraphNode>()
    private val edges = mutableSetOf<ImportGraphEdge>()

    fun addNode(tree: SyntaxTree): Boolean {
        return nodes.put(tree, ImportGraphNode(tree)) == null
    }

    fun addEdge(from: SyntaxTree, to: SyntaxTree, importStatement: ImportStatementSyntax): Boolean {
        val fromNode = nodes.getOrPut(from) { ImportGraphNode(from) }
        val toNode = nodes.getOrPut(to) { ImportGraphNode(to) }
        return edges.add(ImportGraphEdge(fromNode, toNode, importStatement))
    }

    fun getTrees(): List<SyntaxTree> {
        return nodes.values.map { it.tree }
    }

    private val cachedExportedSymbols = mutableMapOf<SyntaxTree, List<Symbol>>()

    fun gatherImportedSymbols(binder: Binder, tree: SyntaxTree): Set<Symbol> {
        val importedSymbols = mutableSetOf<Symbol>()
        val symbolToTreeMap = mutableMapOf<Symbol, SyntaxTree>()
        val treeToImportStatement = mutableMapOf<SyntaxTree, ImportStatementSyntax>()
        val outgoingEdges = getOutgoingEdges(tree)
        for (edge in outgoingEdges) {
            val importedTree = edge.to.tree
            val exportedSymbols = gatherExportedSymbols(binder, importedTree)
            for (symbol in exportedSymbols) {
                symbolToTreeMap[symbol] = importedTree
            }
            val importStatement = edge.importStatementSyntax
            treeToImportStatement[importedTree] = importStatement
            importedSymbols.addAll(exportedSymbols)
        }
        for (importedSymbol1 in importedSymbols) {
            for (importedSymbol2 in importedSymbols) {
                if (importedSymbol1 === importedSymbol2) continue
                if (importedSymbol1.conflictsWith(importedSymbol2)) {
                    val tree1 = symbolToTreeMap[importedSymbol1]!!
                    val tree2 = symbolToTreeMap[importedSymbol2]!!
                    val importStatementSyntax = treeToImportStatement[tree1]!!
                    diagnosticsBag.reportConflictingImport(
                        importedSymbol1,
                        tree1,
                        importedSymbol2,
                        tree2,
                        importStatementSyntax
                    )
                }
            }
        }
        return importedSymbols
    }

    fun gatherExportedSymbols(binder: Binder, tree: SyntaxTree): List<Symbol> {
        val node = nodes[tree]!!
        val exportedSymbols = mutableListOf<Symbol>()
        for (member in node.tree.root.members) {
            when (member) {
                is FunctionDeclarationSyntax -> {
                    val functionSymbol = binder.bindFunctionDeclaration(member, tree, defineSymbol = false)
                    exportedSymbols.add(functionSymbol)
                }

                is GlobalVariableDeclarationSyntax -> {
                    val declaration = binder.bindVariableDeclaration(member.statement, defineSymbol = false)
                    exportedSymbols.add(declaration.variable)
                }

                is ImportStatementSyntax -> {}
            }
        }
        cachedExportedSymbols[tree] = exportedSymbols
        return exportedSymbols
    }

    fun getOutgoingEdges(tree: SyntaxTree): List<ImportGraphEdge> {
        val node = nodes[tree]!!
        return edges.filter { it.from == node }
    }


    fun findCycles(): List<List<ImportGraphNode>> {
        val cycles = mutableListOf<List<ImportGraphNode>>()
        val visited = mutableSetOf<ImportGraphNode>()
        val stack = mutableListOf<ImportGraphNode>()
        for (node in nodes.values) {
            if (node in visited) {
                continue
            }
            findCycles(node, visited, stack, cycles)
        }
        return cycles
    }

    private fun findCycles(
        node: ImportGraphNode,
        visited: MutableSet<ImportGraphNode>,
        stack: MutableList<ImportGraphNode>,
        cycles: MutableList<List<ImportGraphNode>>,
    ) {
        visited.add(node)
        stack.add(node)
        for (edge in edges) {
            if (edge.from != node) {
                continue
            }
            val toNode = edge.to
            if (toNode in stack) {
                val cycle = mutableListOf<ImportGraphNode>()
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