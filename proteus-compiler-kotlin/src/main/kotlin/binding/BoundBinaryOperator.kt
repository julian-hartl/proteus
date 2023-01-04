package binding

import syntax.lexer.SyntaxKind

internal sealed class BoundBinaryOperator(
    val syntaxKind: SyntaxKind, val leftType: BoundType, val rightType: BoundType, val resultType: BoundType
) {


    constructor(syntaxKind: SyntaxKind, type: BoundType) : this(syntaxKind, type, type, type)

    constructor(syntaxKind: SyntaxKind, type: BoundType, resultType: BoundType) : this(
        syntaxKind,
        type,
        type,
        resultType
    )

    companion object {
        private val operators = BoundBinaryOperator::class.sealedSubclasses.map { it.objectInstance!! }

        fun bind(syntaxKind: SyntaxKind, leftType: BoundType, rightType: BoundType): BoundBinaryOperator? {
            return operators.firstOrNull {
                it.syntaxKind == syntaxKind && it.leftType.isAssignableTo(leftType) && it.rightType.isAssignableTo(
                    rightType
                )
            }
        }
    }

    object BoundAdditionBinaryOperator : BoundBinaryOperator(SyntaxKind.PlusToken, BoundType.Int)

    object BoundSubtractionBinaryOperator : BoundBinaryOperator(SyntaxKind.MinusToken, BoundType.Int)

    object BoundMultiplicationBinaryOperator : BoundBinaryOperator(SyntaxKind.AsteriskToken, BoundType.Int)

    object BoundDivisionBinaryOperator : BoundBinaryOperator(SyntaxKind.SlashToken, BoundType.Int)

    object BoundExponentiationBinaryOperator : BoundBinaryOperator(SyntaxKind.DoubleCircumflexToken, BoundType.Int)

    object BoundBitwiseAndBinaryOperator : BoundBinaryOperator(SyntaxKind.AmpersandToken, BoundType.Int)

    object BoundBitwiseOrBinaryOperator : BoundBinaryOperator(SyntaxKind.PipeToken, BoundType.Int)

    object BoundBitwiseLogicalAndBinaryOperator : BoundBinaryOperator(SyntaxKind.AndToken, BoundType.Boolean)

    object BoundBitwiseLogicalOrBinaryOperator : BoundBinaryOperator(SyntaxKind.OrToken, BoundType.Boolean)

    object BoundBitwiseLogicalXorBinaryOperator : BoundBinaryOperator(SyntaxKind.XorToken, BoundType.Boolean)
    object BoundEqualsBinaryOperator :
        BoundBinaryOperator(SyntaxKind.EqualityToken, BoundType.Object, BoundType.Boolean)

    object BoundNotEqualsBinaryOperator :
        BoundBinaryOperator(SyntaxKind.NotEqualityToken, BoundType.Object, BoundType.Boolean)

    object BoundLessThanBinaryOperator : BoundBinaryOperator(SyntaxKind.LessThanToken, BoundType.Int, BoundType.Boolean)

    object BoundGreaterThanBinaryOperator :
        BoundBinaryOperator(SyntaxKind.GreaterThanToken, BoundType.Int, BoundType.Boolean)

    object BoundLessThanOrEqualsBinaryOperator :
        BoundBinaryOperator(SyntaxKind.LessThanOrEqualsToken, BoundType.Int, BoundType.Boolean)

    object BoundGreaterThanOrEqualsBinaryOperator :
        BoundBinaryOperator(SyntaxKind.GreaterThanOrEqualsToken, BoundType.Int, BoundType.Boolean)


}