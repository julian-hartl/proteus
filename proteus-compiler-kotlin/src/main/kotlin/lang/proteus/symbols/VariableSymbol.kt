package lang.proteus.symbols

import lang.proteus.binding.BoundExpression
import lang.proteus.binding.BoundLiteralExpression

internal sealed class VariableSymbol(
    override val name: String,
    val type: TypeSymbol,
    val isFinal: Boolean,
    var constantValue: BoundExpression?,
) : Symbol() {
    override fun toString(): String {
        return "$name: $type"
    }

    val isReadOnly get() = isFinal || isConst

    val isLocal: Boolean
        get() = this is LocalVariableSymbol

    val isParameter: Boolean
        get() = this is ParameterSymbol

    val isGlobal: Boolean
        get() = this is GlobalVariableSymbol

    val isConst: Boolean
        get() = constantValue != null

    val declarationLiteral: String get() = if (isConst) "const" else if (isFinal) "val" else "var"
}

internal class GlobalVariableSymbol(
    name: String,
    type: TypeSymbol,
    isFinal: Boolean,
    constantValue: BoundLiteralExpression<*>? = null,
) : VariableSymbol(
    name, type,
    isFinal, constantValue
)

internal open class LocalVariableSymbol(
    name: String, type: TypeSymbol, isFinal: Boolean,
    constantValue: BoundLiteralExpression<*>? = null,
) :
    VariableSymbol(
        name, type,
        isFinal, constantValue

    )

