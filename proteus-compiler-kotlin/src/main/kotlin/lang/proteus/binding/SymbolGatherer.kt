package lang.proteus.binding

import lang.proteus.grammar.ProteusParser
import lang.proteus.grammar.ProteusParserBaseVisitor
import lang.proteus.symbols.*
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.RuleNode

internal class SymbolGatherer(private val module: Module) {

    public fun gatherSymbols(): List<Symbol> {
        val visitor = SymbolGatherVisitor()
        return visitor.visit(module.compilationUnit)
    }

    private inner class SymbolGatherVisitor : ProteusParserBaseVisitor<List<Symbol>>() {

        override fun visit(tree: ParseTree?): List<Symbol> {
            return super.visit(tree)
        }

        override fun visitChildren(node: RuleNode): List<Symbol> {
            if (node.parent !is ProteusParser.CompilationUnitContext) {
                return emptyList()
            }
            val symbols = mutableListOf<Symbol>()
            for (i in 0 until node!!.childCount) {
                val child = node.getChild(i)
                if (child is RuleNode) {
                    symbols.addAll(visit(child))
                }
            }
            return symbols
        }

        override fun visitImportDeclaration(ctx: ProteusParser.ImportDeclarationContext): List<Symbol> {
            val moduleReference = ModuleReferenceSymbol(ctx.moduleReference().identifier().map { it.text }.toList())
            val importSymbol = ImportSymbol(moduleReference)
            return listOf(importSymbol)
        }

        override fun visitFunctionDeclaration(ctx: ProteusParser.FunctionDeclarationContext): List<Symbol> {
            val name = ctx.identifier().text
            val parameters = ctx.functionParameterList().functionParameter().map {
                val name = it.identifier().text
                val typeIdentifier = it.typeClause().identifier().text
                val typeSymbol = TypeSymbol(typeIdentifier, module.moduleReference)
                ParameterSymbol(name, typeSymbol, module.moduleReference)
            }
            val returnTypeIdentifier = ctx.returnTypeClause()?.identifier()?.text
            val returnTypeSymbol =
                if (returnTypeIdentifier != null) TypeSymbol(returnTypeIdentifier, module.moduleReference) else null
            val functionSymbol = FunctionSymbol(name, parameters, returnTypeSymbol, module.moduleReference)
            return listOf(functionSymbol)
        }

        override fun visitVariableDeclaration(ctx: ProteusParser.VariableDeclarationContext): List<Symbol> {
            val variableSymbol = VariableDeclarationSymbolParser.parse(ctx, module, isGlobal = true)
            return listOf(variableSymbol)
        }

    }


}