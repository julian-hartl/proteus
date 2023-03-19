package lang.proteus.syntax.lexer.token

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.parser.SyntaxTree

internal sealed class Token(open val literal: kotlin.String? = null) {

    object EndOfFile : Token()
    object Identifier : Token()
    object Whitespace : Token()
    object Number : Token()
    object Bad : Token()

    object Expression : Token()
    object Statement : Token()

    object OpenBrace : Token("{")

    object CloseBrace : Token("}")
    object SingleQuote : Token("'")
    object ElseClause : Token()
    object TypeClause : Token()

    object SemiColon : Token(";")

    object Comma : Token(",")
    object Colon : Token(":")

    object Arrow : Token("->")

    object Dot : Token(".")

    object String : Token()
    object Annotation : Token()
    object GlobalStatement : Token()
    object FunctionDeclaration : Token()
    object StructDeclaration : Token()
    object FunctionReturnType : Token()
    object Parameter : Token()
    object ImportStatement : Token()

    object StructMember : Token()

    object StructMemberInitialization : Token()
    object Type : Token()

    fun toSyntaxToken(
        position: Int,
        literal: kotlin.String,
        value: Any? = null,
        syntaxTree: SyntaxTree,
    ): SyntaxToken<Token> {
        return SyntaxToken(this, position, literal, value, syntaxTree)
    }

    override fun toString(): kotlin.String {
        return this::class.simpleName!!
    }



}