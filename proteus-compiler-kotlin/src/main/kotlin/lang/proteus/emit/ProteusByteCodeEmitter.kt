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

    private var generateAsPointer = false

    private val api = ProteusByteCodeApi(boundProgram.structMembers)

    private val currentStackFrame: ProteusStackFrame
        get() = stackFrames.peek()

    override fun generate(): String {
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
        val parameterMap = mutableMapOf<VariableSymbol, Int>()
        var offset = -MemoryLayout.pointerSize
        for (parameter in functionSymbol.parameters) {
            offset -= MemoryLayout.layout(parameter.type, boundProgram.structMembers).sizeInBytes
            parameterMap[parameter] = offset
        }


        stackFrames.push(ProteusStackFrame(parameterMap.toMutableMap(), functionSymbol = functionSymbol))

        writeFunctionDeclaration(functionSymbol)

        codeBuilder.append(api.beginStackFrame(functionSymbol))
        if (functionSymbol.simpleName == "main") {
            writeComment("Global variables")
            allocateGlobalVariables()
        }
        writeComment("Local variables")
        writeComment("Function body")
        generateBlockStatement(body)
    }

    override fun generateDereferenceExpression(expression: BoundDereferenceExpression) {
        // todo: implement
        generateExpression(expression.expression)
        codeBuilder.appendLine(
            api.rload(
                0,
                MemoryLayout.pointerSize
            )
        )

    }

    override fun generateReferenceExpression(expression: BoundReferenceExpression) {
        when (val referencedExpression = expression.expression) {
            is BoundVariableExpression -> {
                val variable = referencedExpression.variable
                val offset = currentStackFrame.variables[variable] ?: globalVariables[variable]!!
                codeBuilder.appendLine(api.loada(offset))
            }

            is BoundLiteralExpression<*>, is BoundUnaryExpression, is BoundBinaryExpression -> {
                generateExpression(referencedExpression)
                val sizeInBytes = if (referencedExpression is BoundLiteralExpression<*>) {
                    when (referencedExpression.value) {
                        is Int -> 4
                        is Boolean -> 4
                        is String -> referencedExpression.value.length + 1
                        else -> throw Exception("Unexpected literal ${referencedExpression.value}")
                    }
                } else MemoryLayout.layout(
                    referencedExpression.type,
                    boundProgram.structMembers
                ).sizeInBytes
                codeBuilder.appendLine(
                    "pushsp -$sizeInBytes"
                )
            }

            is BoundMemberAccessExpression -> {
                generateAsPointer = true
                generateExpression(referencedExpression)
                generateAsPointer = false

            }


            else -> throw Exception("Unexpected expression $referencedExpression")
        }
    }

    private fun writeFunctionDeclaration(functionSymbol: FunctionSymbol) {
        writeComment("${functionSymbol.simpleName}(${functionSymbol.parameters.joinToString(", ") { it.type.simpleName }})")
    }

    private fun allocateGlobalVariables() {
        val globalVariables = boundProgram.globalScope.variables

        if (globalVariables.isEmpty()) return
        codeBuilder.appendLine("halloc ${globalVariables.size * MemoryLayout.pointerSize}")

        var offset = 0
        for (variable in globalVariables) {
            generateAsPointer = true
            generateExpression(boundProgram.variableInitializers[variable]!!)
            generateAsPointer = false
            store(offset, variable.type)
            this.globalVariables[variable] = offset
            offset += MemoryLayout.pointerSize
        }
    }

    override fun generateMemberAccessExpression(expression: BoundMemberAccessExpression) {
        generateAsPointer = true
        generateExpression(expression.expression)
        generateAsPointer = false
        val structName = expression.expression.type.simpleName
        val struct = boundProgram.globalScope.structs.first { it.simpleName == structName }
        val structMembers = boundProgram.structMembers[struct]!!
        val memberIndex = structMembers.indexOfFirst { it.simpleName == expression.memberName }
        val memberOffset = MemoryLayout.layoutStruct(struct, boundProgram.structMembers).offsetInBytes(memberIndex)
        val memberSize = MemoryLayout.layout(expression.type, boundProgram.structMembers).sizeInBytes
        codeBuilder.appendLine(api.rload(memberOffset, memberSize))
    }

    override fun generateStructInitializationExpression(expression: BoundStructInitializationExpression) {
        val memoryLayout = MemoryLayout.layoutStruct(expression.struct, structMemberMap = boundProgram.structMembers)
        if (generateAsPointer) {
            generateAsPointer = false
            codeBuilder.appendLine("halloc ${memoryLayout.sizeInBytes}")
        }
        for (member in expression.members) {
            generateExpression(member.expression)
        }
    }

    override fun generateReturnStatement(statement: BoundReturnStatement) {
        if (statement.expression != null) {
            generateExpression(statement.expression)
        }
        val frame = currentStackFrame
        if (frame.functionSymbol.simpleName == "main") {
            codeBuilder.appendLine(api.halt())
        } else {
            codeBuilder.appendLine(
                api.iret(
                    MemoryLayout.layout(frame.functionSymbol.returnType, boundProgram.structMembers).sizeInBytes
                )
            )
        }

    }

    override fun generateAssignmentExpression(expression: BoundAssignmentExpression) {
        val index = stackFrames.peek().variables[expression.variable]!!
        generateExpression(expression.expression)

        val memoryLayout = MemoryLayout.layout(expression.variable.type, boundProgram.structMembers)
        val sizeInBytes = memoryLayout.sizeInBytes
        codeBuilder.appendLine(api.store(index, sizeInBytes))
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
                        codeBuilder.appendLine(api.iadd())
                    }
                }
            }

            BoundBinaryOperatorKind.Subtraction -> codeBuilder.appendLine(api.isub())
            BoundBinaryOperatorKind.Multiplication -> codeBuilder.appendLine(api.imul())
            BoundBinaryOperatorKind.Division -> codeBuilder.appendLine(api.idiv())
            BoundBinaryOperatorKind.LogicalAnd ->   codeBuilder.appendLine(api.iand())
            BoundBinaryOperatorKind.LogicalOr -> codeBuilder.appendLine(api.ior())
            BoundBinaryOperatorKind.Equality -> codeBuilder.appendLine(api.ieq())
            BoundBinaryOperatorKind.Inequality -> codeBuilder.appendLine(api.ine())
            BoundBinaryOperatorKind.LessThan -> codeBuilder.appendLine(api.ilt())
            BoundBinaryOperatorKind.LessThanOrEqual -> codeBuilder.appendLine(api.ile())
            BoundBinaryOperatorKind.GreaterThan -> codeBuilder.appendLine(api.igt())
            BoundBinaryOperatorKind.GreaterThanOrEqual -> codeBuilder.appendLine(api.ige())
            BoundBinaryOperatorKind.Modulo -> codeBuilder.appendLine(api.imod())
            BoundBinaryOperatorKind.LogicalXor -> codeBuilder.appendLine(api.ixor())
            else -> throw Exception("Unexpected binary operator ${expression.operator.kind}")
        }

    }

    override fun generateCallExpression(expression: BoundCallExpression) {
        for (argument in expression.arguments.reversed()) {
            generateExpression(argument)
        }
        if (expression.function.declaration.isExternal) {

            codeBuilder.appendLine(api.ffcall(expression.function))

        } else {
            codeBuilder.appendLine(api.call(expression.function))
        }
    }

    override fun generateConversionExpression(expression: BoundConversionExpression) {
        generateExpression(expression.expression)
        if (expression.conversion.isIdentity) return
        when (expression.expression.type) {
            TypeSymbol.Int -> {
                when (expression.type) {
                    TypeSymbol.String -> {
                        codeBuilder.appendLine(api.itoa())
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


        when (expression.type) {
            is TypeSymbol.Int -> codeBuilder.appendLine(api.push(expression.value as Int))
            is TypeSymbol.Boolean -> {
                expression.value as Boolean
                codeBuilder.appendLine(api.push(if (expression.value) 1 else 0))
            }

            is TypeSymbol.String -> {
                expression.value as String
                for (char in expression.value) {
                    codeBuilder.appendLine(api.pushb(char.code))
                }
                codeBuilder.appendLine(api.push(0))
            }

            is TypeSymbol.Pointer -> {
                when (expression.value) {
                    is Int -> {
                        codeBuilder.appendLine("halloc ${MemoryLayout.pointerSize}")
                        codeBuilder.appendLine("push ${expression.value}")
                        store(0, TypeSymbol.Int)
                    }

                    is Boolean -> {
                        codeBuilder.appendLine("halloc ${MemoryLayout.pointerSize}")
                        codeBuilder.appendLine("push ${if (expression.value) 1 else 0}")
                        store(0, TypeSymbol.Boolean)
                    }

                    is String -> {
                        val stringSizeInBytes = expression.value.length + 1
                        codeBuilder.appendLine(
                            "halloc $stringSizeInBytes"
                        )
                        codeBuilder.appendLine("push 0")
                        for ((index, char) in expression.value.reversed().withIndex()) {
                            codeBuilder.appendLine("pushb ${char.code}")
                            codeBuilder.appendLine("storeb $index")
                        }
                    }

                    else -> throw Exception("Unexpected literal expression ${expression.value}")
                }
            }

            else -> throw Exception("Unexpected literal expression ${expression.value}")
        }

    }

    override fun generateUnaryExpression(expression: BoundUnaryExpression) {
        generateExpression(expression.operand)
        when (expression.operator) {
            BoundUnaryOperator.BoundUnaryNotOperator -> {
                codeBuilder.appendLine(api.inot())
            }

            BoundUnaryOperator.BoundUnaryNegationOperator -> {
                codeBuilder.appendLine(api.ineg())
            }

            else -> throw Exception("Unexpected unary operator ${expression.operator}")
        }

    }

    override fun generateVariableExpression(expression: BoundVariableExpression) {
        val variable = expression.variable
        if (variable.isGlobal) {

            val memoryAddress = globalVariables[variable]!!
            codeBuilder.appendLine(api.push(memoryAddress))

        } else {
            if (generateAsPointer) {
                codeBuilder.appendLine(api.loada(currentStackFrame.variables[variable]!!))
            } else {
                load(variable)
            }
        }


    }

    override fun generateVariableDeclaration(statement: BoundVariableDeclaration) {
        val index = api.stackFrameSize
        currentStackFrame.variables[statement.variable] = index
        val size = MemoryLayout.layout(statement.variable.type, boundProgram.structMembers).sizeInBytes
        codeBuilder.appendLine(api.alloc(size))
        generateAsPointer = statement.variable.type is TypeSymbol.Pointer
        generateExpression(statement.initializer)
        generateAsPointer = false
        store(index, statement.variable.type)
    }


    override fun generateConditionalGotoStatement(statement: BoundConditionalGotoStatement) {
        generateExpression(statement.condition)
        codeBuilder.appendLine(api.jz(statement.label.name))

    }

    override fun generateGotoStatement(statement: BoundGotoStatement) {
        codeBuilder.appendLine(api.jmp(statement.label.name))
    }

    override fun generateLabelStatement(statement: BoundLabelStatement) {
        codeBuilder.append("${statement.label.name}: ")
    }

    override fun generateNopStatement(statement: BoundNopStatement) {

        codeBuilder.appendLine(api.nop())

    }

    private fun load(variable: VariableSymbol) {
        val index = currentStackFrame.variables[variable]!!
        val layout = MemoryLayout.layout(variable.type, boundProgram.structMembers)
        codeBuilder.appendLine(api.load(index, layout.sizeInBytes))
    }

    private fun store(variable: VariableSymbol) {
        val index = currentStackFrame.variables[variable]!!
        val layout = MemoryLayout.layout(variable.type, boundProgram.structMembers)
        codeBuilder.appendLine(api.store(index, layout.sizeInBytes))
    }

    private fun store(offset: Int, typeSymbol: TypeSymbol) {
        val layout = MemoryLayout.layout(typeSymbol, boundProgram.structMembers)
        codeBuilder.appendLine(api.store(offset, layout.sizeInBytes))
    }

    private fun writeComment(comment: String) {
//        codeBuilder.appendLine("; $comment")
    }
}