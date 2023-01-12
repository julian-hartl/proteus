package lang.proteus.binding

import lang.proteus.syntax.lexer.token.Keyword
import lang.proteus.syntax.lexer.token.Operator

internal data class BoundForStatement(
    val variable: VariableSymbol,
    val lowerBound: BoundExpression,
    val rangeOperator: Keyword,
    val upperBound: BoundExpression,
    val body: BoundStatement
) : BoundStatement()