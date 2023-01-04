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
    Negation, Invert;

    companion object {
        fun fromSyntaxToken(operatorToken: SyntaxToken<*>, operandType: KType): BoundUnaryOperatorKind? {
            val operatorKind = when (operatorToken.kind) {
                SyntaxKind.PlusToken -> Identity
                SyntaxKind.MinusToken -> Negation
                SyntaxKind.NotToken -> Invert
                else -> throw Exception("Unexpected token ${operatorToken.kind}")
            }
            if (operatorKind.allowsType(operandType)) {
                return operatorKind
            }
            return null
        }
    }

    fun allowsType(type: KType): Boolean {
        return when (this) {
            Identity -> type == Int::class.createType()
            Negation -> type == Int::class.createType()
            Invert -> type == Boolean::class.createType()
        }
    }
}


internal class BoundArithmeticBinaryExpression(
    val left: BoundExpression,
    val right: BoundExpression,
    val operatorKind: BoundArithmeticBinaryOperatorKind
) :
    BoundExpression() {

    override val type: KType
        get() = left.type

    override val kind: BoundNodeKind
        get() = BoundNodeKind.ArithmeticBinaryExpression
}

internal enum class BoundArithmeticBinaryOperatorKind {
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
        fun fromSyntaxToken(token: SyntaxToken<*>): BoundArithmeticBinaryOperatorKind? {
            for (operatorKind in values()) {
                if (operatorKind.syntaxKind == token.kind) {
                    return operatorKind
                }
            }
            return null
        }
    }
}

internal class BoundBooleanBinaryExpression(
    val left: BoundExpression,
    val right: BoundExpression,
    val operatorKind: BoundBooleanBinaryOperatorKind
) :
    BoundExpression() {

    override val type: KType
        get() = Boolean::class.createType()

    override val kind: BoundNodeKind
        get() = BoundNodeKind.BooleanBinaryExpression
}

internal enum class BoundBooleanBinaryOperatorKind {
    Or,
    And,
    Xor;

    val syntaxKind: SyntaxKind
        get() = when (this) {
            Or -> SyntaxKind.OrToken
            And -> SyntaxKind.AndToken
            Xor -> SyntaxKind.XorToken
        }

    companion object {
        fun fromSyntaxToken(token: SyntaxToken<*>): BoundBooleanBinaryOperatorKind? {
            for (operatorKind in values()) {
                if (operatorKind.syntaxKind == token.kind) {
                    return operatorKind
                }
            }
            return null
        }
    }
}

internal class BoundGenericBinaryExpression(
    val left: BoundExpression,
    val right: BoundExpression,
    val operatorKind: BoundGenericBinaryOperatorKind
) :
    BoundExpression() {

    override val type: KType
        get() = left.type

    override val kind: BoundNodeKind
        get() = BoundNodeKind.BooleanBinaryExpression
}

internal enum class BoundGenericBinaryOperatorKind {
    Equals;

    val syntaxKind: SyntaxKind
        get() = when (this) {
            Equals -> SyntaxKind.EqualityToken
        }

    companion object {
        fun fromSyntaxToken(token: SyntaxToken<*>): BoundGenericBinaryOperatorKind? {
            for (operatorKind in values()) {
                if (operatorKind.syntaxKind == token.kind) {
                    return operatorKind
                }
            }
            return null
        }
    }
}
