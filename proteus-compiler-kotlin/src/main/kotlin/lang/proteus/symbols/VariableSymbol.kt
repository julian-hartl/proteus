package lang.proteus.symbols

import lang.proteus.binding.BoundExpression
import lang.proteus.binding.BoundLiteralExpression
import lang.proteus.syntax.parser.SyntaxTree
import lang.proteus.syntax.parser.statements.VariableDeclarationSyntax

internal sealed class VariableSymbol(
    name: String,
    val type: TypeSymbol,
    val isFinal: Boolean,
    var constantValue: BoundExpression?,
    syntaxTree: SyntaxTree,
    uniqueIdentifier: String = syntaxTree.id.toString(),
) : Symbol(uniqueIdentifier, name) {
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

    val isConst: Boolean
        get() = constantValue != null

    val declarationLiteral: String get() = if (isConst) "const" else if (isFinal) "val" else "var"
}

internal class GlobalVariableSymbol(
    name: String,
    type: TypeSymbol,
    isFinal: Boolean,
    constantValue: BoundLiteralExpression<*>? = null,
    val declarationSyntax: VariableDeclarationSyntax,
    syntaxTree: SyntaxTree,
) : VariableSymbol(
    name,
    type,
    isFinal,
    constantValue,
    syntaxTree
)

internal open class LocalVariableSymbol(
    name: String, type: TypeSymbol,
    isFinal: Boolean,
    constantValue: BoundLiteralExpression<*>? = null,
    val syntaxTree: SyntaxTree,
    val enclosingFunction: FunctionSymbol,
) :
    VariableSymbol(
        name,
        type,
        isFinal,
        constantValue,
        syntaxTree,
        uniqueIdentifier = enclosingFunction.qualifiedName
    )

