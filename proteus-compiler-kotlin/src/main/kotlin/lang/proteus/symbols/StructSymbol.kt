package lang.proteus.symbols

import lang.proteus.syntax.parser.StructDeclarationSyntax
import lang.proteus.syntax.parser.StructMemberSyntax
import lang.proteus.syntax.parser.SyntaxTree

internal class StructSymbol(
    val declaration: StructDeclarationSyntax,
    val name: String,
    syntaxTree: SyntaxTree,
) : Symbol(
    syntaxTree.id.toString(), declaration.identifier.literal
)

internal class StructMemberSymbol(
    val name: String,
    val type: TypeSymbol,
    val syntax: StructMemberSyntax,
    syntaxTree: SyntaxTree,
): Symbol(syntaxTree.id.toString(), name)