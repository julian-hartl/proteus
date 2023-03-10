package lang.proteus.syntax.parser

import lang.proteus.diagnostics.TextLocation
import lang.proteus.diagnostics.TextSpan
import lang.proteus.syntax.lexer.token.Token
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

internal abstract class SyntaxNode protected constructor(val syntaxTree: SyntaxTree) {
    abstract val token: Token

    // Note: This needs to be a method to prevent it from being called when `this::class.memberProperties` is called in the getChildren method,
    // because that would cause an infinite loop.
    open fun span(): TextSpan {
        val children = getChildren()
        val first = children.firstOrNull()
        val last = children.lastOrNull()
        return if (first != null && last != null) {
            val firstSpan = first.span()
            val lastSpan = last.span()
            TextSpan.fromBounds(firstSpan.start, lastSpan.end)
        } else {
            TextSpan(0, token.literal?.length ?: 0)
        }
    }

    val location: TextLocation
        get() = TextLocation(syntaxTree.sourceText, span())

    fun getChildren(): List<SyntaxNode> {
        val children = mutableListOf<SyntaxNode>()
        val memberProperties = this::class.memberProperties
        val properties: Collection<KProperty1<out SyntaxNode, *>> =
            this::class.primaryConstructor!!.parameters
                .map { parameter ->
                    memberProperties.first { it.name == parameter.name }
                }

        for (property in properties) {
            @Suppress("UNCHECKED_CAST") val property = property as KProperty1<Any, *>
            if (!property.isAccessible) {
                property.isAccessible = true
            }
            val value = property.get(this)
            if (value is SyntaxNode) {
                children.add(value)
            } else if (value is List<*>) {
                for (item in value) {
                    if (item is SyntaxNode) {
                        children.add(item)
                    }
                }
            }
        }
        return children.toList()
    }

    override fun toString(): String {
        return token::class.simpleName!!
    }
}