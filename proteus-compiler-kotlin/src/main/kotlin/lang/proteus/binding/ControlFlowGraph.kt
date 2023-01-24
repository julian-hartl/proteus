package lang.proteus.binding

import lang.proteus.generation.CodeGenerator
import java.io.File
import java.time.Instant
import java.util.*

internal class ControlFlowGraph private constructor(
    val start: BasicBlock,
    val end: BasicBlock,
    val blocks: List<BasicBlock>,
    val branches: List<BasicBlockBranch>,
) {
    data class BasicBlock(
        val isStart: Boolean? = null,
        val statements: MutableList<BoundStatement> = mutableListOf(),
        val incoming: MutableList<BasicBlock> = mutableListOf(),
        val outgoing: MutableList<BasicBlock> = mutableListOf(),
    ) {
        val isEnd: Boolean?
            get() = if (isStart == null) null else !isStart

        override fun hashCode(): Int {
            return statements.hashCode()
        }

        override fun toString(): String {
            val builder = StringBuilder()
            if (isStart == true) {
                builder.appendLine("<Start>")
            }
            if (isEnd == true) {
                builder.appendLine("<End>")
            }

            for (statement in statements) {
                val asString = CodeGenerator.generate(statement, functions = setOf())
                builder.appendLine(asString)
            }
            return builder.toString()
        }
    }

    data class BasicBlockBranch(val from: BasicBlock, val to: BasicBlock, val condition: BoundExpression?) {
        override fun toString(): String {
            if (condition == null) {
                return ""
            }
            return CodeGenerator.generate(BoundExpressionStatement(condition), functions = setOf())
        }
    }

    private fun findPath(from: BasicBlock, to: BasicBlock): List<BasicBlock>? {
        val visited = mutableSetOf<BasicBlock>()
        val stack = Stack<BasicBlock>()
        stack.push(from)
        while (stack.isNotEmpty()) {
            val current = stack.pop()
            if (current == to) {
                stack.push(current)
                return stack
            }
            visited.add(current)
            for (next in current.outgoing) {
                if (next !in visited) {
                    stack.push(next)
                }
            }
        }
        return null
    }

    fun allPathsReturn(): Boolean {
        for (block in end.incoming) {
            val path = findPath(start, block) ?: return false
            if (path.any { it.isEnd == true }) {
                return false
            }
        }

        return end.incoming.isNotEmpty()
    }

    fun outputAsGraphviz(): String {
        val sb = StringBuilder()
        sb.appendLine("digraph FlowGraph{")
        val blockIds = blocks.mapIndexed { index, block -> block to "N$index" }.toMap()
        for (block in blocks) {
            val id = blockIds[block]!!
            val label = block.toString().replace("\"", "\\\"")
            sb.appendLine("    $id [label = \"$label\" shape = box]")
        }
        for (branch in branches) {
            val fromId = blockIds[branch.from]!!
            val toId = blockIds[branch.to]!!
            val label = if (branch.condition == null) "" else branch.toString()
            sb.appendLine("    $fromId -> $toId [label = \"$label\"]")
        }
        sb.appendLine("}")
        return sb.toString()
    }

    companion object {
        fun createAndOutput(cfgStatement: BoundBlockStatement): ControlFlowGraph {
            val cfg = create(cfgStatement)
            output(cfg)
            return cfg
        }

        fun create(cfgStatement: BoundBlockStatement): ControlFlowGraph {


            val blockBuilder = BasicBlockBuilder(cfgStatement)
            blockBuilder.build()
            val blocks = blockBuilder.blocks

            val graphBuilder = GraphBuilder()
            return graphBuilder.build(blocks)


        }

        fun allPathsReturn(body: BoundBlockStatement, output: Boolean = false): Boolean {
            val graph = if (output) createAndOutput(body) else create(body)

            return graph.allPathsReturn()
        }

        private val path get() = "graphs/cfg_${Instant.now().toEpochMilli()}.dot"

        private fun output(cfg: ControlFlowGraph): String {
            val output = cfg.outputAsGraphviz()
            val file = File(path)
            file.parentFile.mkdirs()
            file.writeText(output)
            return path
        }

        class GraphBuilder {
            private val branches: MutableList<BasicBlockBranch> = mutableListOf()
            private val blockFromStatement: MutableMap<BoundStatement, BasicBlock> = mutableMapOf()
            private val blockFromLabel: MutableMap<BoundLabel, BasicBlock> = mutableMapOf()
            lateinit var end: BasicBlock
            fun build(blocks: MutableList<BasicBlock>): ControlFlowGraph {
                val start = BasicBlock(true)
                end = BasicBlock(false);
                if (!blocks.any()) {
                    connect(start, end)
                } else {
                    connect(start, blocks.first())
                    connect(blocks.last(), end)
                }
                for (block in blocks) {
                    for (statement in block.statements) {
                        blockFromStatement[statement] = block
                        if (statement is BoundLabelStatement) {
                            blockFromLabel[statement.label] = block
                        }
                    }
                }

                for ((index, block) in blocks.withIndex()) {
                    val next = if (index == blocks.size - 1) end else blocks[index + 1]
                    for (statement in block.statements) {
                        val isLastStatement = statement == block.statements.last()
                        walk(statement, block, next, isLastStatement)
                    }
                }
                scanAgain@
                for (block in blocks) {
                    if (!block.incoming.any()) {
                        removeBlock(block)
                        continue@scanAgain
                    }
                }

                blocks.add(0, start)
                blocks.add(end)
                return ControlFlowGraph(start, end, blocks, branches)
            }

            private fun removeBlock(block: BasicBlock) {
                for (incoming in block.incoming) {
                    incoming.outgoing.remove(block)
                }
                for (outgoing in block.outgoing) {
                    outgoing.incoming.remove(block)
                }
                branches.removeAll { it.from == block || it.to == block }
            }

            private fun walk(
                statement: BoundStatement,
                block: BasicBlock,
                next: BasicBlock,
                lastStatement: Boolean,
            ) {
                when (statement) {
                    is BoundGotoStatement -> {
                        val target = blockFromLabel[statement.label]!!
                        connect(block, target)
                    }

                    is BoundConditionalGotoStatement -> {
                        val thenBlock = blockFromLabel[statement.label]!!
                        val negatedCondition = negate(statement.condition)
                        val thenCondition = if (statement.jumpIfFalse) {
                            negatedCondition
                        } else {
                            statement.condition
                        }
                        val elseCondition = if (statement.jumpIfFalse) {
                            statement.condition
                        } else {
                            negatedCondition
                        }
                        connect(block, thenBlock, thenCondition)
                        connect(block, next, elseCondition)
                    }

                    is BoundLabelStatement -> {
                        // do nothing
                    }

                    is BoundReturnStatement -> {
                        connect(block, end)
                    }

                    is BoundVariableDeclaration, is BoundExpressionStatement -> {
                        if (lastStatement) {
                            connect(block, next)
                        }
                    }

                    is BoundNopStatement -> {
                        // do nothing
                    }

                    else -> throw Exception("Unexpected statement $statement")
                }
            }

            private fun negate(condition: BoundExpression): BoundExpression {
                if (condition is BoundLiteralExpression<*>) {
                    return BoundLiteralExpression(!(condition.value as Boolean))
                }
                return BoundUnaryExpression(condition, BoundUnaryOperator.BoundUnaryNotOperator)
            }

            private fun connect(from: BasicBlock, to: BasicBlock, condition: BoundExpression? = null) {
                if (condition != null && isContradictingCondition(condition)) {
                    return
                }
                from.outgoing.add(to)
                to.incoming.add(from)
                branches.add(BasicBlockBranch(from, to, condition))
            }

            private fun isContradictingCondition(condition: BoundExpression): Boolean {
                if (condition is BoundLiteralExpression<*>) {
                    return !(condition.value as Boolean)
                }
                return false
            }
        }


        class BasicBlockBuilder(val statement: BoundBlockStatement) {
            val blocks = mutableListOf<BasicBlock>()
            val statements = mutableListOf<BoundStatement>()
            fun build(): List<BasicBlock> {
                val boundStatements = statement.statements
                for (statement in boundStatements) {
                    when (statement) {

                        is BoundVariableDeclaration, is BoundExpressionStatement, is BoundNopStatement -> {
                            statements.add(statement)
                        }

                        is BoundGotoStatement, is BoundReturnStatement, is BoundConditionalGotoStatement -> {
                            statements.add(statement)
                            startBlock()
                        }

                        is BoundLabelStatement -> {
                            startBlock()
                            statements.add(statement)
                        }


                        else -> throw Exception("Unexpected statement $statement")
                    }
                }
                endBlock()

                return blocks
            }

            private fun endBlock() {
                if (statements.size > 0) {
                    val block = BasicBlock()
                    block.statements.addAll(statements)
                    blocks.add(block)
                    statements.clear()
                }
            }

            private fun startBlock() {
                endBlock()
            }
        }
    }

}