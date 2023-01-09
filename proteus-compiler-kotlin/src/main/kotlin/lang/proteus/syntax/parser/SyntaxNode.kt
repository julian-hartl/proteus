package lang.proteus.syntax.parser

import lang.proteus.diagnostics.TextSpan
import lang.proteus.syntax.lexer.token.Token
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

abstract class SyntaxNode {
    abstract val token: Token

    // Note: This needs to be a method to prevent it from being called when `this::class.memberProperties` is called in the getChildren method,
    // because that would cause an infinite loop.
    open fun span(): TextSpan {
        val first = getChildren().firstOrNull()
        val last = getChildren().lastOrNull()
        return if (first != null && last != null) {
            TextSpan.fromBounds(first.span().start, last.span().end)
        } else {
            TextSpan(0, token.literal?.length ?: 0)
        }
    }

    fun getChildren(): List<SyntaxNode> {
        val children = mutableListOf<SyntaxNode>()

        val properties: Collection<KProperty1<out SyntaxNode, *>> = this::class.memberProperties

        for (property in properties) {
            val value = (property as KProperty1<Any, *>).get(this)
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