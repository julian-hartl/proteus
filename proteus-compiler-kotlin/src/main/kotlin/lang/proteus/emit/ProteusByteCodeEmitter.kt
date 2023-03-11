package lang.proteus.emit

import lang.proteus.binding.*
import lang.proteus.symbols.FunctionSymbol
import lang.proteus.symbols.TypeSymbol
import lang.proteus.symbols.VariableSymbol
import java.io.File
import java.util.*

private data class ProteusStackFrame(
    val variables: MutableMap<VariableSymbol, Int>,
    val functionSymbol: FunctionSymbol,
)

internal class ProteusByteCodeEmitter(boundProgram: BoundProgram) : Emitter<String>(boundProgram) {

    companion object {
        fun emit(boundProgram: BoundProgram, outputPath: String) {
            val code = ProteusByteCodeEmitter(boundProgram).generate()
            val outputFile = File(outputPath)
            outputFile.parentFile.mkdirs()
            outputFile.writeText(code)
            println("Generated code written to ${outputFile.absolutePath}")
        }
    }

    private val codeBuilder = StringBuilder()

    private val stackFrames = Stack<ProteusStackFrame>()

    private val globalVariables = mutableMapOf<VariableSymbol, Int>()

    private val currentStackFrame: ProteusStackFrame
        get() = stackFrames.peek()

    override fun generate(): String {
        allocateGlobalVariables()
        generateFunction(boundProgram.mainFunction!!)
        for (functionSymbol in boundProgram.globalScope.functions.filter {
            it.simpleName != "main"
        }) {
            generateFunction(functionSymbol)
        }
        return codeBuilder.toString()
    }

    override fun generateFunction(functionSymbol: FunctionSymbol) {
        val body = boundProgram.functionBodies[functionSymbol] ?: return
        val parameterMap = functionSymbol.parameters.sortedBy {
            it.simpleName
        }.mapIndexed { index, symbol ->
            symbol to -(index + 2)
        }.toMap()


        stackFrames.push(ProteusStackFrame(parameterMap.toMutableMap(), functionSymbol = functionSymbol))

        codeBuilder.append("${functionSymbol.qualifiedName}: ")
        allocateScopedVariables(body)
        generateBlockStatement(body)
    }

    private fun allocateScopedVariables(boundBlockStatement: BoundBlockStatement) {
        val frame = currentStackFrame
        var index = 0
        for (statement in boundBlockStatement.statements) {
            if (statement is BoundVariableDeclaration) {
                frame.variables[statement.variable] = index++
            }
        }
        codeBuilder.appendLine("alloc $index")
    }

    private fun allocateGlobalVariables() {
        val globalVariables = boundProgram.globalScope.variables

        codeBuilder.appendLine("halloc ${globalVariables.size * MemoryLayout.pointerSize}")

        var offset = 0
        for (variable in globalVariables) {
            generateExpression(boundProgram.variableInitializers[variable]!!)
            codeBuilder.appendLine("hstore $offset")
            this.globalVariables[variable] = offset
            offset += MemoryLayout.pointerSize
        }
    }

    override fun generateMemberAccessExpression(expression: BoundMemberAccessExpression) {
        generateExpression(expression.expression)
        val structName = expression.expression.type.simpleName
        val struct = boundProgram.globalScope.structs.first { it.simpleName == structName }
        val structMembers = boundProgram.structMembers[struct]!!
        val memberIndex = structMembers.indexOfFirst { it.simpleName == expression.memberName }
        val memberOffset = MemoryLayout.layoutStruct(struct, boundProgram.structMembers).offsetInBytes(memberIndex)
        codeBuilder.appendLine("hload $memberOffset")
    }

    override fun generateStructInitializationExpression(expression: BoundStructInitializationExpression) {
        val memoryLayout = MemoryLayout.layoutStruct(expression.struct, structMemberMap = boundProgram.structMembers)
        codeBuilder.appendLine("halloc ${memoryLayout.sizeInBytes}")
        for ((index, member) in expression.members.withIndex()) {

            generateExpression(member.expression)
            val memberOffset = memoryLayout.offsetInBytes(index)
            codeBuilder.appendLine("hstore $memberOffset")
        }
    }

    override fun generateReturnStatement(statement: BoundReturnStatement) {
        if (statement.expression != null) {
            generateExpression(statement.expression)
        }
        val frame = currentStackFrame
        if (frame.functionSymbol.simpleName == "main") {
            codeBuilder.appendLine("halt")
        } else {
            codeBuilder.appendLine("iret")
        }

    }

    override fun generateAssignmentExpression(expression: BoundAssignmentExpression) {
        val index = stackFrames.peek().variables[expression.variable]!!
        generateExpression(expression.expression)

        codeBuilder.appendLine("store $index")
    }

    override fun generateBinaryExpression(expression: BoundBinaryExpression) {
        generateExpression(expression.right)
        generateExpression(expression.left)
        when (expression.operator.kind) {
            BoundBinaryOperatorKind.Addition -> {
                when (expression.type) {
                    is TypeSymbol.String -> {
                        codeBuilder.appendLine("sadd")
                    }

                    else -> {
                        codeBuilder.appendLine("iadd")
                    }
                }
            }

            BoundBinaryOperatorKind.Subtraction -> codeBuilder.appendLine("isub")
            BoundBinaryOperatorKind.Multiplication -> codeBuilder.appendLine("imul")
            BoundBinaryOperatorKind.Division -> codeBuilder.appendLine("idiv")
            BoundBinaryOperatorKind.LogicalAnd -> codeBuilder.appendLine("iand")
            BoundBinaryOperatorKind.LogicalOr -> codeBuilder.appendLine("ior")
            BoundBinaryOperatorKind.Equality -> codeBuilder.appendLine("ieq")
            BoundBinaryOperatorKind.Inequality -> codeBuilder.appendLine("ine")
            BoundBinaryOperatorKind.LessThan -> codeBuilder.appendLine("ilt")
            BoundBinaryOperatorKind.LessThanOrEqual -> codeBuilder.appendLine("ile")
            BoundBinaryOperatorKind.GreaterThan -> codeBuilder.appendLine("igt")
            BoundBinaryOperatorKind.GreaterThanOrEqual -> codeBuilder.appendLine("ige")
            BoundBinaryOperatorKind.Modulo -> codeBuilder.appendLine("imod")
            BoundBinaryOperatorKind.LogicalXor -> codeBuilder.appendLine("ixor")
            else -> throw Exception("Unexpected binary operator ${expression.operator.kind}")
        }

    }

    override fun generateCallExpression(expression: BoundCallExpression) {
        for (argument in expression.arguments.reversed()) {
            generateExpression(argument)
        }
        if (expression.function.declaration.isExternal) {

            codeBuilder.appendLine("ffcall ${expression.function.simpleName}")

        } else {
            codeBuilder.appendLine("call ${expression.function.qualifiedName}")
        }
    }

    override fun generateConversionExpression(expression: BoundConversionExpression) {
        generateExpression(expression.expression)
        if (expression.conversion.isIdentity) return
        when (expression.expression.type) {
            TypeSymbol.Int -> {
                when (expression.type) {
                    TypeSymbol.String -> {
                        codeBuilder.appendLine("itoa")
                    }

                    else -> {
                        throw Exception("Unexpected conversion from ${expression.expression.type} to ${expression.type}")
                    }
                }
            }

            else -> {
                throw Exception("Unexpected conversion from ${expression.expression.type} to ${expression.type}")
            }
        }
    }

    override fun generateLiteralExpression(expression: BoundLiteralExpression<*>) {
        when (expression.value) {
            is Int -> codeBuilder.appendLine("push ${expression.value}")
            is Boolean -> codeBuilder.appendLine("push ${if (expression.value) 1 else 0}")
            is String -> {
                val stringSizeInBytes = expression.value.length + 1
                codeBuilder.appendLine(
                    "halloc $stringSizeInBytes"
                )
                for ((index, char) in expression.value.withIndex()) {
                    codeBuilder.appendLine("pushb ${char.code}")
                    codeBuilder.appendLine("hstoreb $index")
                }
            }

            else -> throw Exception("Unexpected literal expression ${expression.value}")
        }
    }

    override fun generateUnaryExpression(expression: BoundUnaryExpression) {
        generateExpression(expression.operand)
        when (expression.operator) {
            BoundUnaryOperator.BoundUnaryNotOperator -> {
                codeBuilder.appendLine("inot")
            }

            BoundUnaryOperator.BoundUnaryNegationOperator -> {
                codeBuilder.appendLine("ineg")
            }

            else -> throw Exception("Unexpected unary operator ${expression.operator}")
        }

    }

    override fun generateVariableExpression(expression: BoundVariableExpression) {
        val variable = expression.variable
        if (variable.isGlobal) {

            val memoryAddress = globalVariables[variable]!!
            codeBuilder.appendLine("push $memoryAddress")

        } else {
            val index = stackFrames.peek().variables[expression.variable]!!
            codeBuilder.appendLine("load $index")
        }


    }

    override fun generateVariableDeclaration(statement: BoundVariableDeclaration) {
        val index = currentStackFrame.variables[statement.variable]!!
        generateExpression(statement.initializer)
        codeBuilder.appendLine("store $index")
    }


    override fun generateConditionalGotoStatement(statement: BoundConditionalGotoStatement) {
        generateExpression(statement.condition)
        codeBuilder.appendLine("jz ${statement.label.name}")

    }

    override fun generateGotoStatement(statement: BoundGotoStatement) {
        codeBuilder.appendLine("jmp ${statement.label.name}")
    }

    override fun generateLabelStatement(statement: BoundLabelStatement) {
        codeBuilder.append("${statement.label.name}: ")
    }

    override fun generateNopStatement(statement: BoundNopStatement) {

    }
}