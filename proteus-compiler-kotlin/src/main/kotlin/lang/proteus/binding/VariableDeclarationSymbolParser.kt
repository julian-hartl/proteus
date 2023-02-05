package lang.proteus.binding

import lang.proteus.grammar.ProteusParser.VariableDeclarationContext
import lang.proteus.symbols.GlobalVariableSymbol
import lang.proteus.symbols.LocalVariableSymbol
import lang.proteus.symbols.TypeSymbol
import lang.proteus.symbols.VariableSymbol

internal object VariableDeclarationSymbolParser {
    fun parse(ctx: VariableDeclarationContext, module: Module, isGlobal: Boolean): VariableSymbol {
        val variableModifier = ctx.variableModifier()?.text
        val name = ctx.identifier().text
        val typeIdentifier = ctx.typeClause()?.identifier()?.text
        val typeSymbol = if (typeIdentifier != null) TypeSymbol(typeIdentifier) else null
        val isFinal = variableModifier == "val"
        val isConst = variableModifier == "const"
        val variableSymbol = if (isGlobal) GlobalVariableSymbol(
            name,
            typeSymbol,
            null,
            isFinal,
            isConst,
            module.moduleReference
        ) else LocalVariableSymbol(
            name,
            typeSymbol,
            null,
            isFinal,
            isConst,
            module.moduleReference
        )
        return variableSymbol
    }
}