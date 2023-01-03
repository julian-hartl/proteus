package binding

import syntax.lexer.SyntaxKind
import syntax.lexer.SyntaxToken
import kotlin.reflect.KType
import kotlin.reflect.full.createType

sealed class BoundExpression : BoundNode() {
    abstract val type: KType
}

internal class BoundLiteralExpression<T : Any>(val value: T) : BoundExpression() {
    override val kind: BoundNodeKind
        get() = BoundNodeKind.LiteralExpression

    override val type: KType
        get() = value::class.createType()
}

internal class BoundUnaryExpression(val operand: BoundExpression, val operatorKind: BoundUnaryOperatorKind) :
    BoundExpression() {

    override val type: KType
        get() = operand.type

    override val kind: BoundNodeKind
        get() = BoundNodeKind.UnaryExpression
}

internal enum class BoundUnaryOperatorKind {
    Identity,
    Negation, ;

    companion object {
        fun fromSyntaxToken(operatorToken: SyntaxToken<*>): BoundUnaryOperatorKind {
            return when (operatorToken.kind) {
                SyntaxKind.PlusToken -> Identity
                SyntaxKind.MinusToken -> Negation
                else -> throw Exception("Unexpected token ${operatorToken.kind}")
            }
        }
    }
}

internal class BoundBinaryExpression(
    val left: BoundExpression,
    val right: BoundExpression,
    val operatorKind: BoundBinaryOperatorKind
) :
    BoundExpression() {

    override val type: KType
        get() = left.type

    override val kind: BoundNodeKind
        get() = BoundNodeKind.BinaryExpression
}

internal enum class BoundBinaryOperatorKind {
    Addition,
    Subtraction,
    Division,
    Multiplication,
    LogicalAnd,
    LogicalOr;

    val syntaxKind: SyntaxKind
        get() = when (this) {
            Addition -> SyntaxKind.PlusToken
            Subtraction -> SyntaxKind.MinusToken
            Division -> SyntaxKind.SlashToken
            Multiplication -> SyntaxKind.AsteriskToken
            LogicalAnd -> SyntaxKind.AmpersandToken
            LogicalOr -> SyntaxKind.PipeToken
        }

    companion object {
        fun fromSyntaxToken(token: SyntaxToken<*>): BoundBinaryOperatorKind {
            for (operatorKind in values()) {
                if (operatorKind.syntaxKind == token.kind) {
                    return operatorKind
                }
            }
            throw Exception("Unexpected token ${token.kind}")
        }
    }
}
