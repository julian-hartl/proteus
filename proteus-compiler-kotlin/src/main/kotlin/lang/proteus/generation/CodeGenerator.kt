package lang.proteus.generation

import lang.proteus.binding.*
import lang.proteus.symbols.FunctionSymbol
import lang.proteus.symbols.TypeSymbol
import java.io.File
import java.time.Instant

internal class CodeGenerator private constructor(
    private val codeBuilder: StringBuilder = StringBuilder(),
    private val functionBodies: Map<FunctionSymbol, BoundBlockStatement>,
    private val functions: Set<FunctionSymbol>,
) :
    BoundTreeRewriter() {

    companion object {
        fun generate(
            functionBodies: Map<FunctionSymbol, BoundBlockStatement> = mapOf(),
            functions: Set<FunctionSymbol>,
        ): String {
            val generator = CodeGenerator(functionBodies = functionBodies, functions = functions)
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
        generateFunctionDeclarations(functions)
        return codeBuilder.toString()
    }

    private fun generateFunctionDeclarations(functions: Set<FunctionSymbol>) {
        for (symbol in functions) {
            val declaration = symbol.declaration
            for (modifier in declaration.modifiers) {
                codeBuilder.append("${modifier.literal} ")
            }
            codeBuilder.append("fn ${symbol.name}(")
            for (parameter in declaration.parameters) {
                codeBuilder.append("${parameter.identifier.literal}: ${parameter.typeClause.type.literal}, ")
            }

            if (declaration.parameters.count != 0) {
                codeBuilder.delete(codeBuilder.length - 2, codeBuilder.length)
            }
            codeBuilder.append(") ")
            codeBuilder.append("-> ${declaration.returnTypeClause?.type?.literal ?: "Unit"}")
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
        codeBuilder.append(node.variable.name)
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


    override fun rewriteBlockStatement(node: BoundBlockStatement): BoundStatement {
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
        codeBuilder.append(expression.functionSymbol.name)
        codeBuilder.append("(")
        for (argument in expression.arguments) {
            rewriteExpression(argument)
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
        codeBuilder.append(expression.type.name)

        return expression
    }

    override fun rewriteReturnStatement(statement: BoundReturnStatement): BoundStatement {
        codeBuilder.append("return")
        if (statement.boundExpression != null) {
            codeBuilder.append(" ")
            rewriteExpression(statement.boundExpression)
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
        codeBuilder.append(expression.variable.name)
        return expression
    }

    override fun rewriteVariableDeclaration(node: BoundVariableDeclaration): BoundStatement {
        codeBuilder.append("var ")
        codeBuilder.append(node.variable.name)
        codeBuilder.append(": ")
        codeBuilder.append(node.variable.type.name)
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

}