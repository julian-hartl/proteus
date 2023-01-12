package lang.proteus.binding

import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

internal sealed class BoundNode {

    fun getChildren(): List<BoundNode> {
        val children = mutableListOf<BoundNode>()

        val properties: Collection<KProperty1<out BoundNode, *>> = this::class.memberProperties

        for (property in properties) {
            val property = property as KProperty1<Any, *>
            if (!property.isAccessible) {
                property.isAccessible = true
            }
            val value = property.get(this)
            if (value is BoundNode) {
                children.add(value)
            } else if (value is List<*>) {
                for (item in value) {
                    if (item is BoundNode) {
                        children.add(item)
                    }
                }
            }
        }
        return children.toList()
    }
}
