package lang.proteus.lowering

import lang.proteus.binding.*
import lang.proteus.syntax.lexer.token.Operator

internal class Lowerer private constructor() : BoundTreeRewriter() {
    companion object {
        fun lower(statement: BoundStatement): BoundStatement {
            val lowerer = Lowerer()
            return lowerer.rewriteStatement(statement)
        }
    }

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


}