package lang.proteus.syntax.parser.statements

import lang.proteus.syntax.parser.ExpressionSyntax
import lang.proteus.syntax.parser.SyntaxTree

internal class ExpressionStatementSyntax(val expression: ExpressionSyntax, syntaxTree: SyntaxTree) : StatementSyntax(
    syntaxTree
) {

}