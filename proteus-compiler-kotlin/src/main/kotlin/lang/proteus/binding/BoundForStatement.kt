package lang.proteus.binding

import lang.proteus.symbols.LocalVariableSymbol
import lang.proteus.symbols.VariableSymbol
import lang.proteus.syntax.lexer.token.Keyword

internal data class BoundForStatement(
    val variable: LocalVariableSymbol,
    val lowerBound: BoundExpression,
    val rangeOperator: Keyword,
    val upperBound: BoundExpression,
    val body: BoundStatement
) : BoundStatement()