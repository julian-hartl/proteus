package lang.proteus.generation

import lang.proteus.binding.*
import lang.proteus.symbols.LocalVariableSymbol
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

    private var ifCount = 0;

    private data class Loop(val breakLabel: BoundLabel, val continueLabel: BoundLabel)

    private val loopStack: Stack<Loop> = Stack()
    private val whileCount
        get() = loopStack.size

    /*
    * for <var> in <iterator>
    *   <body>;
    *
    * -->
    * {
    *
    *   var <random_uuid> = <lower>;
    *   while <random_uuid> <= <upper> {
    *       var <var> = <random_uuid>;
    *       <random_uuid> += 1;
    *       <body>;
    *   }
    * }
     */
    override fun rewriteForStatement(node: BoundForStatement): BoundStatement {
        val randomUuid = UUID.randomUUID().toString()
        val loopCounterVariable = LocalVariableSymbol("loop_counter_${randomUuid}", node.variable.type, false)
        val loopCounterVariableDeclaration = BoundVariableDeclaration(loopCounterVariable, node.lowerBound)
        val loopValueVariableDeclaration = BoundVariableDeclaration(node.variable, BoundVariableExpression(loopCounterVariable))
        val increment = BoundAssignmentExpression(
            loopCounterVariable,
            BoundLiteralExpression(1),
            Operator.PlusEquals,
            returnAssignment = false
        )
        val condition = BoundBinaryExpression(
            BoundVariableExpression(loopCounterVariable),
            node.upperBound,
            BoundBinaryOperator.bind(Operator.LessThanEquals, loopCounterVariable.type, node.upperBound.type)!!,
        )
        val whileStatement = BoundWhileStatement(
            condition,
            BoundBlockStatement(listOf(loopValueVariableDeclaration, BoundExpressionStatement(increment), node.body))
        )
        val result = BoundBlockStatement(
            listOf(
                loopCounterVariableDeclaration,
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
            val endLabel = generateLabel("if_${ifCount++}_end")
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
        val labelIfCount = ifCount++
        val elseLabel = generateLabel("else_${labelIfCount}")
        val goToElse = BoundConditionalGotoStatement(node.condition, elseLabel, jumpIfFalse = true)
        val endLabel = generateLabel("if_${labelIfCount}_end")
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
        val condition = rewriteExpression(node.condition)
        val checkLabel = generateLabel("while_${whileCount}_condition")
        val endLabel = generateLabel("while_${whileCount}_end")
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
        loopStack.push(Loop(endLabel, checkLabel))
        val statement = rewriteStatement(result)
        loopStack.pop()
        return statement
    }

    private val labelSet = mutableSetOf<BoundLabel>()

    private fun generateLabel(name: String): BoundLabel {
        val label = BoundLabel(name)
        val isLegal = labelSet.add(label)
        if (!isLegal) {
            throw IllegalStateException("Label $name is already used")
        }
        return label
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

    /*
    * We should rewrite arithmetic assignment expressions to binary expressions
    * and assignment expressions.
    * For example:
    * a += b
    * -->
    * a = a + b
     */
    override fun rewriteAssignmentExpression(node: BoundAssignmentExpression): BoundExpression {
        return when (node.assignmentOperator) {
            Operator.Equals -> super.rewriteAssignmentExpression(node)
            Operator.MinusEquals -> rewriteArithmeticAssignmentExpression(node, Operator.Minus)
            Operator.PlusEquals -> rewriteArithmeticAssignmentExpression(node, Operator.Plus)
        }
    }

    private fun rewriteArithmeticAssignmentExpression(
        node: BoundAssignmentExpression,
        operator: Operator,
    ): BoundExpression {
        val left = BoundVariableExpression(node.variable)
        val right = node.expression
        val operator = BoundBinaryOperator.bind(operator, node.variable.type, node.variable.type)
        val binaryExpression = BoundBinaryExpression(left, right, operator!!)
        return BoundAssignmentExpression(node.variable, binaryExpression, Operator.Equals, node.returnAssignment)
    }

    override fun rewriteBreakStatement(statement: BoundBreakStatement): BoundStatement {
        val loop = loopStack.peek()
        val result = BoundGotoStatement(loop.breakLabel)
        return rewriteStatement(result)
    }

    override fun rewriteContinueStatement(statement: BoundContinueStatement): BoundStatement {
        val loop = loopStack.peek()
        val continueLabel = loop.continueLabel
        val result = BoundGotoStatement(continueLabel)
        return rewriteStatement(result)
    }
}