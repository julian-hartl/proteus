package lang.proteus.generation

import lang.proteus.binding.*
import lang.proteus.symbols.FunctionSymbol
import lang.proteus.symbols.GlobalVariableSymbol
import lang.proteus.symbols.StructSymbol
import lang.proteus.symbols.TypeSymbol
import java.io.File
import java.time.Instant

internal class CodeGenerator private constructor(
    private val functionBodies: Map<FunctionSymbol, BoundBlockStatement>,
    private val functions: Set<FunctionSymbol>,
    private val globalVariables: Set<GlobalVariableSymbol>,
    private val globalVariableInitializers: Map<GlobalVariableSymbol, BoundExpression>,
    private val structs: Set<StructSymbol> ,
) :
    BoundTreeRewriter() {
    private val codeBuilder: StringBuilder = StringBuilder()

    companion object {
        fun generate(
            boundProgram: BoundProgram,
        ): String {
            val generator = CodeGenerator(
                functionBodies = boundProgram.functionBodies,
                functions = boundProgram.globalScope.functions,
                globalVariables = boundProgram.globalScope.globalVariables,
                globalVariableInitializers = boundProgram.variableInitializers,
                structs = boundProgram.globalScope.structs.toSet(),
            )
            return generator.generateCode()
        }

        fun emitGeneratedCode(code: String) {
            val outputDirectory = System.getProperty("user.dir") + "/generated"
            val outputFile = File(outputDirectory, "generated_${Instant.now().toEpochMilli()}.psl")
            outputFile.parentFile.mkdirs()
            outputFile.writeText(code)
            println("Generated code written to ${outputFile.absolutePath}")
        }
    }

    private fun generateCode(): String {
        generateGlobalVariables(globalVariables)
        generateFunctionDeclarations(functions)
        generateStructDeclarations(structs)
        return codeBuilder.toString()
    }

    private fun generateStructDeclarations(structs: Set<StructSymbol>) {
        for (symbol in structs) {
            codeBuilder.appendLine("struct ${symbol.qualifiedName} {")
            for (field in symbol.declaration.members) {
                codeBuilder.appendLine("    ${field.identifier.literal}: ${field.type.type.typeIdentifier.literal};")
            }
            codeBuilder.appendLine("}")
        }
    }

    private fun generateGlobalVariables(globalVariables: Set<GlobalVariableSymbol>) {
        for (symbol in globalVariables) {
            val declarationLiteral = symbol.declarationLiteral
            codeBuilder.append("$declarationLiteral ${symbol.qualifiedName}: ${symbol.type.simpleName}")
            val initializer = globalVariableInitializers[symbol]!!
            codeBuilder.append(" = ")
            rewriteExpression(initializer)
            codeBuilder.appendLine(";")
        }
    }

    private fun generateFunctionDeclarations(functions: Set<FunctionSymbol>) {
        for (symbol in functions) {
            val declaration = symbol.declaration
            for (modifier in declaration.modifiers) {
                codeBuilder.append("${modifier.literal} ")
            }
            codeBuilder.append("fn ${symbol.qualifiedName}(")
            for (parameter in symbol.parameters) {

                codeBuilder.append("${parameter.qualifiedName}: ${parameter.type.simpleName}, ")
            }

            if (declaration.parameters.count != 0) {
                codeBuilder.delete(codeBuilder.length - 2, codeBuilder.length)
            }
            codeBuilder.append(") ")
            codeBuilder.append("-> ${declaration.returnTypeClause?.type?.typeIdentifier?.literal ?: "Unit"}")
            val body = functionBodies[symbol]
            if (body != null) {
                codeBuilder.append(" ")
                rewriteBlockStatement(body)
            } else {
                codeBuilder.appendLine(";")
            }
        }
    }


    override fun rewriteAssignmentExpression(node: BoundAssignmentExpression): BoundExpression {
        rewriteExpression(node.assignee.expression)
        codeBuilder.append(" ")
        codeBuilder.append(node.assignmentOperator.literal)
        codeBuilder.append(" ")
        rewriteExpression(node.expression)
        return node
    }

    override fun rewriteBinaryExpression(node: BoundBinaryExpression): BoundExpression {
        codeBuilder.append("(")
        codeBuilder.append("(")
        rewriteExpression(node.left)
        codeBuilder.append(") ")
        codeBuilder.append(node.operator.operator.literal)
        codeBuilder.append(" (")
        rewriteExpression(node.right)
        codeBuilder.append(")")
        codeBuilder.append(")")
        return node
    }


    override fun rewriteBlockStatement(node: BoundBlockStatement): BoundBlockStatement {
        codeBuilder.append("{")
        codeBuilder.appendLine()
        for (statement in node.statements) {
            codeBuilder.append("    ")
            rewriteStatement(statement)
            codeBuilder.appendLine()
        }
        codeBuilder.append("}")
        codeBuilder.appendLine()
        return node
    }

    override fun rewriteCallExpression(expression: BoundCallExpression): BoundExpression {
        codeBuilder.append(expression.function.qualifiedName)
        codeBuilder.append("(")
        for ((index, argument) in expression.arguments.withIndex()) {
            rewriteExpression(argument)
            if (index < expression.arguments.size - 1) {
                codeBuilder.append(", ")
            }
        }
        codeBuilder.append(")")
        return expression
    }

    override fun rewriteConditionalGotoStatement(statement: BoundConditionalGotoStatement): BoundStatement {
        codeBuilder.append("gotoIfFalse ")
        codeBuilder.append(statement.label)
        codeBuilder.append(" ")
        rewriteExpression(statement.condition)
        codeBuilder.append(";")
        return statement
    }

    override fun rewriteConversionExpression(expression: BoundConversionExpression): BoundExpression {
        rewriteExpression(expression.expression)
        codeBuilder.append(" as ")
        codeBuilder.append(expression.type.simpleName)

        return expression
    }

    override fun rewriteReturnStatement(statement: BoundReturnStatement): BoundStatement {
        codeBuilder.append("return")
        if (statement.expression != null) {
            codeBuilder.append(" ")
            rewriteExpression(statement.expression)
        }
        codeBuilder.append(";")
        return statement
    }

    override fun rewriteGotoStatement(statement: BoundGotoStatement): BoundStatement {
        codeBuilder.append("goto ")
        codeBuilder.append(statement.label)
        codeBuilder.append(";")
        return statement
    }

    override fun rewriteNopStatement(statement: BoundNopStatement): BoundStatement {
        codeBuilder.append("nop;")
        return statement
    }

    override fun rewriteLiteralExpression(expression: BoundLiteralExpression<*>): BoundExpression {
        val value =
            when (expression.type) {
                is TypeSymbol.String -> "\"${expression.value}\""
                else -> expression.value
            }
        codeBuilder.append(value)
        return expression
    }

    override fun rewriteUnaryExpression(node: BoundUnaryExpression): BoundExpression {
        codeBuilder.append(node.operator.operator.literal)
        codeBuilder.append("(")
        rewriteExpression(node.operand)
        codeBuilder.append(")")
        return node
    }

    override fun rewriteLabelStatement(statement: BoundLabelStatement): BoundStatement {
        codeBuilder.append(statement.label)
        codeBuilder.append(":")
        return statement
    }

    override fun rewriteVariableExpression(expression: BoundVariableExpression): BoundExpression {
        codeBuilder.append(expression.variable.qualifiedName)
        return expression
    }

    override fun rewriteVariableDeclaration(node: BoundVariableDeclaration): BoundStatement {
        codeBuilder.append(node.variable.declarationLiteral)
        codeBuilder.append(" ")
        codeBuilder.append(node.variable.qualifiedName)
        codeBuilder.append(": ")
        codeBuilder.append(node.variable.type.simpleName)
        codeBuilder.append(" = ")
        rewriteExpression(node.initializer)
        codeBuilder.append(";")
        return node
    }

    override fun rewriteExpressionStatement(statement: BoundExpressionStatement): BoundStatement {
        rewriteExpression(statement.expression)
        codeBuilder.append(";")
        return statement
    }

    override fun rewriteStructInitializationExpression(expression: BoundStructInitializationExpression): BoundExpression {
        codeBuilder.append(expression.struct.qualifiedName)
        codeBuilder.append(" {")
        for ((index, field) in expression.members.withIndex()) {
            codeBuilder.append(field.name)
            codeBuilder.append(": ")
            rewriteExpression(field.expression)
            if (index < expression.members.size - 1) {
                codeBuilder.append(", ")
            }
        }
        codeBuilder.append("}")
        return expression
    }

}