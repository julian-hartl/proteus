package lang.proteus.symbols

import lang.proteus.syntax.parser.SyntaxTree

internal class ParameterSymbol(name: String, type: TypeSymbol,  syntaxTree: SyntaxTree,  enclosingFunction: FunctionSymbol) :
    LocalVariableSymbol(name, type, isFinal = true, syntaxTree = syntaxTree, enclosingFunction = enclosingFunction)