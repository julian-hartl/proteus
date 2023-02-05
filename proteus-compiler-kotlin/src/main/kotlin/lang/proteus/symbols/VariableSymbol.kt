package lang.proteus.symbols

import lang.proteus.binding.BoundExpression
import lang.proteus.binding.BoundLiteralExpression

internal sealed class VariableSymbol(
    name: String,
    val specifiedType: TypeSymbol?,
    val inferredType: TypeSymbol?,
    val isFinal: Boolean,
    val isConst: Boolean,
    moduleReferenceSymbol: ModuleReferenceSymbol,
) : Symbol(name, moduleReferenceSymbol) {

    val type: TypeSymbol get() = specifiedType ?: inferredType!!

    override fun toString(): String {
        return "$simpleName: $type"
    }

    val isReadOnly get() = isFinal || isConst

    val isLocal: Boolean
        get() = this is LocalVariableSymbol

    val isParameter: Boolean
        get() = this is ParameterSymbol

    val isGlobal: Boolean
        get() = this is GlobalVariableSymbol

    val declarationLiteral: String get() = if (isConst) "const" else if (isFinal) "val" else "var"
}

internal class GlobalVariableSymbol(
    name: String,
    specifiedType: TypeSymbol? = null,
    inferredType: TypeSymbol? = null,
    isFinal: Boolean,
    isConst: Boolean, moduleReferenceSymbol: ModuleReferenceSymbol,
) : VariableSymbol(
    name,
    specifiedType = specifiedType,
    inferredType = inferredType,
    isFinal,
    isConst,
    moduleReferenceSymbol,
)

internal open class LocalVariableSymbol(
    name: String,
    specifiedType: TypeSymbol?,
    inferredType: TypeSymbol?,
    isFinal: Boolean,
    isConst: Boolean,
    moduleReferenceSymbol: ModuleReferenceSymbol,
) :
    VariableSymbol(
        name,
        specifiedType,
        inferredType,
        isFinal,
        isConst,
        moduleReferenceSymbol,
    )

