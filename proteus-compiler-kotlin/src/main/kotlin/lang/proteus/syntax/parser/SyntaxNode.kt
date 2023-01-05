package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.Token
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

abstract class SyntaxNode {
    abstract val token: Token
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