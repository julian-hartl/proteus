package lang.proteus.symbols

import lang.proteus.syntax.parser.SyntaxTree

internal class ParameterSymbol(name: String, mutable: Boolean, type: TypeSymbol,  syntaxTree: SyntaxTree,  enclosingFunction: FunctionSymbol) :
    LocalVariableSymbol(name, type, isMut = mutable, syntaxTree = syntaxTree, enclosingFunction = enclosingFunction)