package lang.proteus.emit

import lang.proteus.binding.*
import lang.proteus.diagnostics.Diagnostics
import lang.proteus.diagnostics.MutableDiagnostics
import lang.proteus.ffi.JvmFFI
import lang.proteus.metatdata.Metadata.JAVA_MAIN_CLASS_NAME
import lang.proteus.symbols.FunctionSymbol
import lang.proteus.syntax.lexer.token.ComparisonOperator

class MyClassLoader(private val bytecode: ByteArray) : ClassLoader() {
    override fun findClass(name: String): Class<*> {
        if (name == JAVA_MAIN_CLASS_NAME)
            return defineClass(name, bytecode, 0, bytecode.size)
        return getSystemClassLoader().loadClass(name)
    }
}

internal class JVMEmitter private constructor(
    boundProgram: BoundProgram,
) : Emitter<ByteArray>(boundProgram) {
    companion object {
        internal fun emit(
            boundProgram: BoundProgram,
            outputPath: String,
        ): Diagnostics {
            if (boundProgram.diagnostics.hasErrors()) {
                return boundProgram.diagnostics
            }
            val emitter = JVMEmitter(boundProgram)
            val byteCode = emitter.generate()


            val fos = java.io.FileOutputStream(outputPath)
            fos.write(byteCode)
            fos.close()
            try {
                val classLoader = MyClassLoader(byteCode)
                val clazz = classLoader.loadClass(JAVA_MAIN_CLASS_NAME)
                val mainMethod = clazz.getMethod(boundProgram.mainFunction!!.qualifiedName)
                mainMethod.invoke(null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            println("Generated bytecode written to $outputPath")
            return MutableDiagnostics()
        }
    }


    val jvmClass = JvmBytecodeClass(JAVA_MAIN_CLASS_NAME)

    override fun generate(): ByteArray {
        jvmClass.startGeneration()
        for (function in boundProgram.globalScope.functions) {
            if (function.declaration.isExternal) continue
            generateFunction(function)
        }
        jvmClass.endGeneration()
        return jvmClass.toByteCode()
    }


    override fun generateFunction(functionSymbol: FunctionSymbol) {
        val body = boundProgram.functionBodies[functionSymbol]
            ?: throw IllegalStateException("No body found for function $functionSymbol")
        jvmClass.startMethod(functionSymbol)
        generateStatement(body)
        jvmClass.endMethod()

    }

    override fun generateDereferenceExpression(expression: BoundDereferenceExpression) {
        TODO("Not yet implemented")
    }

    override fun generateReferenceExpression(expression: BoundReferenceExpression) {
        TODO("Not yet implemented")
    }

    override fun generateMemberAccessExpression(expression: BoundMemberAccessExpression) {
        TODO("Not yet implemented")
    }

    override fun generateStructInitializationExpression(expression: BoundStructInitializationExpression) {

    }

    override fun generateReturnStatement(statement: BoundReturnStatement) {
        val expression = statement.expression
        if (expression != null) {
            generateExpression(expression)
        }
        jvmClass.addReturn(statement.returnType)
    }

    override fun generateConditionalGotoStatement(statement: BoundConditionalGotoStatement) {
        generateExpression(statement.condition)
        jvmClass.conditionalJump(statement.label, statement.jumpIfFalse)
    }

    override fun generateGotoStatement(statement: BoundGotoStatement) {
        jvmClass.jump(statement.label)

    }

    override fun generateLabelStatement(statement: BoundLabelStatement) {
        jvmClass.markWithLabel(statement.label)
    }

    override fun generateNopStatement(statement: BoundNopStatement) {
        jvmClass.nop()
    }

    override fun generateVariableDeclaration(statement: BoundVariableDeclaration) {
        val variable = statement.variable
        val initializer = statement.initializer
        jvmClass.defineLocalVariable(variable)
        generateExpression(initializer)
        jvmClass.pop(variable)
    }

    override fun generateLiteralExpression(expression: BoundLiteralExpression<*>) {
        jvmClass.push(expression.value)
    }

    override fun generateUnaryExpression(expression: BoundUnaryExpression) {
        generateExpression(expression.operand)
        val operator = expression.operator.operator
        jvmClass.unaryOperator(operator)
    }

    override fun generateCallExpression(expression: BoundCallExpression) {
        if (expression.function.declaration.isExternal) {
            val ffi = JvmFFI(this)
            ffi.call(expression.function.simpleName, expression.arguments)
            return
        }
        for (argument in expression.arguments) {
            generateExpression(argument)
        }
        jvmClass.call(expression.function)
    }

    override fun generateAssignmentExpression(expression: BoundAssignmentExpression) {
        generateExpression(expression.expression)
        jvmClass.pop(expression.variable)
    }

    override fun generateVariableExpression(expression: BoundVariableExpression) {
        jvmClass.push(expression.variable)
    }

    override fun generateBinaryExpression(expression: BoundBinaryExpression) {


        val operator = expression.operator.operator
        if (operator.isComparisonOperator) {
            operator as ComparisonOperator
            generateExpression(expression.left)
            generateExpression(expression.right)
            val isLeftPointer = JVMSymbols.isPointer(expression.left.type)
            val isRightPointer = JVMSymbols.isPointer(expression.right.type)
            val isPointerComparison = isLeftPointer || isRightPointer
            jvmClass.compare(operator, isPointerComparison)
        } else {
            generateExpression(expression.left)
            generateExpression(expression.right)
            jvmClass.binaryOperator(operator)
        }

    }

    override fun generateConversionExpression(expression: BoundConversionExpression) {
        TODO()
    }


}