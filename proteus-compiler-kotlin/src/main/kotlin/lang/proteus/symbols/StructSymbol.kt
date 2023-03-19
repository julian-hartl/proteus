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
    val syntaxTree: SyntaxTree,
): Symbol(syntaxTree.id.toString(), name) {
    fun copy(
        name: String = this.name,
        type: TypeSymbol = this.type,
        syntax: StructMemberSyntax = this.syntax,
    ) = StructMemberSymbol(name, type, syntax, syntaxTree)
}