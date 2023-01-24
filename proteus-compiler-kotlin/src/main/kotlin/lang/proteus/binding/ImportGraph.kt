package lang.proteus.binding

import lang.proteus.syntax.parser.SyntaxTree

internal data class ImportGraphNode(
    val tree: SyntaxTree,
)

internal data class ImportGraphEdge(
    val from: ImportGraphNode,
    val to: ImportGraphNode,
)

internal class ImportGraph {
    private val nodes = mutableMapOf<SyntaxTree, ImportGraphNode>()
    private val edges = mutableSetOf<ImportGraphEdge>()

    private val pathToNodeMap = mutableMapOf<String, ImportGraphNode>()


    fun addEdge(from: SyntaxTree, to: SyntaxTree, importPath: String): Boolean {
        val fromNode = nodes.getOrPut(from) { ImportGraphNode(from) }
        val toNode = nodes.getOrPut(to) { ImportGraphNode(to) }
        pathToNodeMap[importPath] = toNode
        return edges.add(ImportGraphEdge(fromNode, toNode))
    }


    fun getPathToTreeMap(): Map<String, SyntaxTree> {
        return pathToNodeMap.mapValues { it.value.tree }
    }

    fun getTrees(): List<SyntaxTree> {
        return nodes.values.map { it.tree }
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