package lang.proteus.lowering

import lang.proteus.binding.BoundStatement
import lang.proteus.binding.BoundTreeRewriter

internal class Lowerer private constructor() : BoundTreeRewriter() {
    companion object {
        fun lower(statement: BoundStatement): BoundStatement {
            val lowerer = Lowerer()
            return lowerer.rewriteStatement(statement)
        }
    }

}