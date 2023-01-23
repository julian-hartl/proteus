package lang.proteus.binding

internal data class ImportGraphNode(
    val fileName: String,
)

internal data class ImportGraphEdge(
    val from: ImportGraphNode,
    val to: ImportGraphNode,
)

internal class ImportGraph {
    private val nodes = mutableMapOf<String, ImportGraphNode>()
    private val edges = mutableSetOf<ImportGraphEdge>()

    fun addEdges(from: String, to: List<String>) {
        for (toFile in to) {
            addEdge(from, toFile)
        }
    }

    fun addEdge(from: String, to: String): Boolean {
        val fromNode = nodes.getOrPut(from) { ImportGraphNode(from) }
        val toNode = nodes.getOrPut(to) { ImportGraphNode(to) }
        return edges.add(ImportGraphEdge(fromNode, toNode))
    }

    fun getNodes(): List<ImportGraphNode> {
        return nodes.values.toList()
    }

    fun getEdges(): List<ImportGraphEdge> {
        return edges.toList()
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