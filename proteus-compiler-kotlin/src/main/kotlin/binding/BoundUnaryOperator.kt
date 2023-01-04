package binding

import syntax.lexer.SyntaxKind
import kotlin.reflect.KType
import kotlin.reflect.full.createType

internal sealed class BoundUnaryOperator(
    val syntaxKind: SyntaxKind,
    val operandType: BoundType,
    val resultType: BoundType
) {

    constructor(syntaxKind: SyntaxKind, type: BoundType) : this(
        syntaxKind,
        type,
        type
    )

    companion object {
        private val operators = BoundUnaryOperator::class.sealedSubclasses.map { it.objectInstance!! }

        fun bind(syntaxKind: SyntaxKind, operandType: BoundType): BoundUnaryOperator? {
            return operators.firstOrNull { it.syntaxKind == syntaxKind && it.operandType == operandType }
        }
    }

    object BoundUnaryNotOperator : BoundUnaryOperator(SyntaxKind.NotToken, BoundType.Boolean)
    object BoundUnaryIdentityOperator : BoundUnaryOperator(SyntaxKind.PlusToken, BoundType.Int)
    object BoundUnaryNegationOperator : BoundUnaryOperator(SyntaxKind.MinusToken, BoundType.Int)

}