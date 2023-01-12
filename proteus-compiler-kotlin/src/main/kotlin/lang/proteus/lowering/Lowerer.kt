package lang.proteus.lowering

import lang.proteus.binding.*
import lang.proteus.syntax.lexer.token.Operator
import java.util.*

internal class Lowerer private constructor() : BoundTreeRewriter() {
    companion object {
        fun lower(statement: BoundStatement): BoundBlockStatement {
            val lowerer = Lowerer()
            val result = lowerer.rewriteStatement(statement)
            return lowerer.flatten(result)
        }
    }

    private var labelCount = 0;

    /*
    * for <var> in <iterator>
    *   <body>;
    *
    * -->
    * {
    *   var <var> = <lower>;
    *   while <var> <= <upper> {
    *       <body>;
    *       <var> += 1;
    *   }
    * }
     */
    override fun rewriteForStatement(node: BoundForStatement): BoundStatement {
        val variableDeclaration = BoundVariableDeclaration(node.variable, node.lowerBound)
        val condition = BoundBinaryExpression(
            BoundVariableExpression(node.variable),
            node.upperBound,
            BoundBinaryOperator.bind(Operator.LessThanEquals, node.variable.type, node.variable.type)!!,
        )
        val increment = BoundAssignmentExpression(
            node.variable,
            BoundLiteralExpression(1),
            Operator.PlusEquals,
        )
        val whileStatement = BoundWhileStatement(
            condition,
            BoundBlockStatement(listOf(node.body, BoundExpressionStatement(increment)))
        )
        val result = BoundBlockStatement(
            listOf(
                variableDeclaration,
                whileStatement,
            ),
        )
        return rewriteStatement(result)
    }

    /*
    * if <condition>
    *   <then>
    *
    * -->
    *
    * gotoIfFalse <condition> end
    * <then>
    * end:
    *
    * ============================
    * if <condition>
    *   <then>
    * else
    *   <else>
    *
    * -->
    *
    * gotoIfFalse <condition> else
    * <then>
    * goto end
    * else:
    * <else>
    * end:
     */
    override fun rewriteIfStatement(node: BoundIfStatement): BoundStatement {
        if (node.elseStatement == null) {
            val endLabel = generateLabel()
            val gotoFalse = BoundConditionalGotoStatement(node.condition, endLabel, jumpIfFalse = true)
            val endLabelStatement = BoundLabelStatement(endLabel)
            val result = BoundBlockStatement(
                listOf(
                    gotoFalse,
                    node.thenStatement,
                    endLabelStatement
                ),
            )
            return rewriteStatement(result)
        }
        val elseLabel = generateLabel()
        val goToElse = BoundConditionalGotoStatement(node.condition, elseLabel, jumpIfFalse = true)
        val endLabel = generateLabel()
        val goToEnd = BoundGotoStatement(endLabel)
        val elseLabelStatement = BoundLabelStatement(elseLabel)
        val endLabelStatement = BoundLabelStatement(endLabel)
        val result = BoundBlockStatement(
            listOf(
                goToElse,
                node.thenStatement,
                goToEnd,
                elseLabelStatement,
                node.elseStatement,
                endLabelStatement,
            ),
        )
        return rewriteStatement(result)
    }

    /*
    * while <condition>
    *   <body>
    *
    * -->
    *
    * check:
    * gotoIfFalse <condition> end
    * <body>
    * goto check
    * end:
     */
    override fun rewriteWhileStatement(node: BoundWhileStatement): BoundStatement {
        val condition = node.condition
        val checkLabel = generateLabel()
        val endLabel = generateLabel()
        val checkLabelStatement = BoundLabelStatement(checkLabel)
        val goToEnd = BoundConditionalGotoStatement(condition, endLabel, jumpIfFalse = true)
        val gotoCheck = BoundGotoStatement(checkLabel)
        val endLabelStatement = BoundLabelStatement(endLabel)
        val result = BoundBlockStatement(
            listOf(
                checkLabelStatement,
                goToEnd,
                node.body,
                gotoCheck,
                endLabelStatement,
            ),
        )
        return rewriteStatement(result)
    }

    private fun generateLabel(): LabelSymbol {
        val name = "label${labelCount++}"
        return LabelSymbol(name)
    }

    private fun flatten(statement: BoundStatement): BoundBlockStatement {
        val statements = mutableListOf<BoundStatement>()
        val stack = Stack<BoundStatement>()
        stack.push(statement)

        while (!stack.isEmpty()) {
            val current = stack.pop();
            if (current is BoundBlockStatement) {
                for (s in current.statements.reversed()) {
                    stack.push(s)
                }
            } else {
                statements.add(current)
            }
        }

        return BoundBlockStatement(statements.toList())
    }


}