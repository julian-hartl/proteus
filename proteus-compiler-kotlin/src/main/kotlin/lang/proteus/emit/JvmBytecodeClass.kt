package lang.proteus.emit

import lang.proteus.binding.BoundLabel
import lang.proteus.metatdata.Metadata
import lang.proteus.symbols.FunctionSymbol
import lang.proteus.symbols.ParameterSymbol
import lang.proteus.symbols.TypeSymbol
import lang.proteus.symbols.VariableSymbol
import lang.proteus.syntax.lexer.token.ComparisonOperator
import lang.proteus.syntax.lexer.token.Operator
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.util.*

internal class JvmBytecodeClass(private val qualifiedName: String) {

    private val classWriter = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)

    // stores the index of the local variable in the stack frame
    private val stackFrames: Stack<StackFrame> = Stack()

    private lateinit var currentMethod: MethodVisitor

    private val stackFrame: StackFrame
        get() = stackFrames.peek()

    fun startGeneration() {
        classWriter.visit(
            Opcodes.V1_8, Opcodes.ACC_PUBLIC,
            qualifiedName, null, "java/lang/Object", null
        )
    }

    fun startMethod(functionSymbol: FunctionSymbol) {
        val methodName = functionSymbol.qualifiedName
        val descriptor = buildFunctionDescriptor(functionSymbol)
        currentMethod = classWriter.visitMethod(
            Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC,
            methodName,
            descriptor,
            null,
            null
        )
        beginStackFrame(functionSymbol)
        currentMethod.visitCode()
    }

    fun endMethod() {
        endStackFrame()
        currentMethod.visitMaxs(-1, -1)
        currentMethod.visitEnd()
    }

    private fun beginStackFrame(functionSymbol: FunctionSymbol): StackFrame {
        val stackFrame = StackFrame(functionSymbol)
        stackFrames.push(stackFrame)
        for (parameter in functionSymbol.parameters) {
            defineLocalVariable(parameter)
        }
        return stackFrame
    }

    private fun endStackFrame() {
        stackFrames.pop()
    }


    fun defineLocalVariable(variable: VariableSymbol) {
        stackFrame.defineLocalVariable(variable)
        addDebugInfo(variable)
    }

    private fun addDebugInfo(variableSymbol: VariableSymbol) {
        val variableName = variableSymbol.qualifiedName
        val variableIndex = stackFrame.getLocalVariableIndex(variableSymbol)
        val startLabel = stackFrame.startLabel
        val endLabel =stackFrame.endLabel
        currentMethod.visitLocalVariable(
            variableName,
            buildTypeDescriptor(variableSymbol.type),
            null,
            startLabel,
            endLabel,
            variableIndex
        )
    }

    fun jump(label: BoundLabel) {
        val labelValue = stackFrame.lookupOrDefineLabel(label)
        currentMethod.visitJumpInsn(Opcodes.GOTO, labelValue)
    }

    fun conditionalJump(label: BoundLabel, condition: Boolean) {
        val labelValue = stackFrame.lookupOrDefineLabel(label)
        val jumpOpcode = if (condition) Opcodes.IFNE else Opcodes.IFEQ
        currentMethod.visitJumpInsn(jumpOpcode, labelValue)
    }

    fun pop(variableSymbol: VariableSymbol) {
        val index = stackFrame.lookupOrDefineLocalVariable(variableSymbol)
        val storeOpCode = getStoreOpCode(variableSymbol.type)
        currentMethod.visitVarInsn(storeOpCode, index)
    }

    /*
          * Generates something like this:
          * iload    var1
          iload    var2
          if_icmpeq label
          iconst_0
          goto end
          label:
          iconst_1
          end:
          */
    fun compare(comparisonOperator: ComparisonOperator, isPointerComparison: Boolean) {
        val comparisonLabelTrue = Label()
        val comparisonLabelFalse = Label()
        val jumpOpcode = getJumpOpcode(comparisonOperator, isPointerComparison)
        val falseValue = 0
        val trueValue = 1
        currentMethod.visitJumpInsn(jumpOpcode, comparisonLabelTrue)
        currentMethod.visitLdcInsn(falseValue)
        currentMethod.visitJumpInsn(Opcodes.GOTO, comparisonLabelFalse)
        currentMethod.visitLabel(comparisonLabelTrue)
        currentMethod.visitLdcInsn(trueValue)
        currentMethod.visitLabel(comparisonLabelFalse)
    }

    private fun getJumpOpcode(comparisonOperator: ComparisonOperator, isPointerComparison: Boolean): Int {
        if (isPointerComparison) {
            return when (comparisonOperator) {
                Operator.DoubleEquals -> Opcodes.IF_ACMPEQ
                Operator.NotEquals -> Opcodes.IF_ACMPNE
                else -> throw Exception("Invalid comparison operator for pointer comparison")
            }
        }
        return when (comparisonOperator) {
            Operator.DoubleEquals -> Opcodes.IF_ICMPEQ
            Operator.GreaterThan -> Opcodes.IF_ICMPGT
            Operator.GreaterThanEquals -> Opcodes.IF_ICMPGE
            Operator.LessThan -> Opcodes.IF_ICMPLT
            Operator.LessThanEquals -> Opcodes.IF_ICMPLE
            Operator.NotEquals -> Opcodes.IF_ICMPNE
        }
    }

    fun addReturn(returnType: TypeSymbol) {
        val returnOpcode = getReturnOpCode(returnType)
        currentMethod.visitInsn(returnOpcode)
    }

    private fun getReturnOpCode(returnTypeSymbol: TypeSymbol): Int {
        if(returnTypeSymbol is TypeSymbol.Unit) {
            return Opcodes.RETURN
        }
        val isPointer = JVMSymbols.isPointer(returnTypeSymbol)
        if (isPointer) {
            return Opcodes.ARETURN
        }
        return Opcodes.IRETURN
    }


    private fun buildFunctionDescriptor(functionSymbol: FunctionSymbol): String {
        val descriptorBuilder = StringBuilder()
        descriptorBuilder.append("(")
        val parameterDescriptor = buildParameterDescriptor(functionSymbol.parameters)
        descriptorBuilder.append(parameterDescriptor)
        descriptorBuilder.append(")")
        val returnTypeDescriptor = buildTypeDescriptor(functionSymbol.returnType)
        descriptorBuilder.append(returnTypeDescriptor)
        return descriptorBuilder.toString()
    }

    private fun buildParameterDescriptor(parameters: List<ParameterSymbol>): String {
        val descriptorBuilder = StringBuilder()
        for (parameter in parameters) {
            val parameterTypeDescriptor = buildTypeDescriptor(parameter.type)
            descriptorBuilder.append(parameterTypeDescriptor)
        }
        return descriptorBuilder.toString()
    }

    fun markWithLabel(label: BoundLabel) {
        val labelValue = stackFrame.lookupOrDefineLabel(label)
        currentMethod.visitLabel(labelValue)
    }

    fun call(functionSymbol: FunctionSymbol) {
        val functionName = functionSymbol.qualifiedName
        val descriptor = buildFunctionDescriptor(functionSymbol)
        if (functionSymbol.declaration.isExternal) {
            TODO("Rethink how to handle external functions")
        } else {

            currentMethod.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                Metadata.JAVA_MAIN_CLASS_NAME,
                functionName,
                descriptor,
                false
            )
        }
    }

    fun endGeneration() {
        classWriter.visitEnd()
    }

    fun push(value: Any) {
        if (value is VariableSymbol) {
            loadVariable(value)
        } else {
            currentMethod.visitLdcInsn(value)
        }
    }

    private fun loadVariable(value: VariableSymbol) {
        val index = stackFrame.lookupOrDefineLocalVariable(value)
        val loadOpCode = getLoadOpCode(value.type)
        currentMethod.visitVarInsn(loadOpCode, index)
    }

    fun toByteCode(): ByteArray {
        return classWriter.toByteArray()
    }

    private fun buildTypeDescriptor(type: TypeSymbol): String {
        val typeDescriptor = buildJvmType(type)
        if (typeDescriptor.length == 1) {
            return typeDescriptor
        }
        return "$typeDescriptor;"
    }

    private fun getStoreOpCode(typeSymbol: TypeSymbol): Int {
        val isPointer = JVMSymbols.isPointer(typeSymbol)
        if (isPointer) {
            return Opcodes.ASTORE
        }
        return Opcodes.ISTORE
    }

    private fun getLoadOpCode(typeSymbol: TypeSymbol): Int {
        val isPointer = JVMSymbols.isPointer(typeSymbol)
        if (isPointer) {
            return Opcodes.ALOAD
        }
        return Opcodes.ILOAD
    }

    private fun buildJvmType(type: TypeSymbol): String {
        val valueType = JVMSymbols.valueTypeSymbols[type]
        if (valueType != null) {
            return valueType
        }
        val primitiveType = JVMSymbols.primitiveTypeSymbols[type]
        if (primitiveType != null) {
            return primitiveType
        }
        throw IllegalArgumentException("Type $type is not defined.")
    }

    fun binaryOperator(operator: Operator) {
        val opcode = getBinaryOperatorOpcode(operator)
        currentMethod.visitInsn(opcode)
    }

    private fun getBinaryOperatorOpcode(operator: Operator): Int {
        return when (operator) {
            Operator.Plus -> Opcodes.IADD
            Operator.Minus -> Opcodes.ISUB
            Operator.Asterisk -> Opcodes.IMUL
            Operator.Slash -> Opcodes.IDIV
            Operator.Percent -> Opcodes.IREM
            Operator.Pipe -> Opcodes.IOR
            Operator.Ampersand -> Opcodes.IAND
            Operator.Circumflex -> Opcodes.IXOR
            Operator.And -> Opcodes.IAND
            Operator.Or -> Opcodes.IOR
            Operator.Xor -> Opcodes.IXOR
            else -> throw Exception("Invalid operator for binary operator")
        }
    }

    fun unaryOperator(operator: Operator) {
        val opcode = getUnaryOperatorOpcode(operator)
        currentMethod.visitInsn(opcode)
    }

    private fun getUnaryOperatorOpcode(operator: Operator): Int {
        return when (operator) {
            Operator.Not -> Opcodes.INEG
            Operator.Minus -> Opcodes.INEG
            else -> throw Exception("Invalid operator for unary operator")
        }
    }

    fun nop() {
        currentMethod.visitInsn(Opcodes.NOP)
    }

}