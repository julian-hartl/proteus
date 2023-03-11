package lang.proteus.emit

import lang.proteus.symbols.StructMemberSymbol
import lang.proteus.symbols.StructSymbol
import lang.proteus.symbols.TypeSymbol

data class MemoryLayout(
    val memberSizesInBytes: List<Int>,
    val alignmentInBytes: Int,
) {

    val sizeInBytes: Int = memberSizesInBytes.sum()

    fun offsetInBytes(memberIndex: Int): Int = memberSizesInBytes.subList(0, memberIndex).sum()

    companion object {
        internal fun layoutStruct(
            struct: StructSymbol,
            structMemberMap: Map<StructSymbol, Set<StructMemberSymbol>>,
        ): MemoryLayout {
            val memberSizesInBytes = mutableListOf<Int>()
            var alignment = 0
            for (member in structMemberMap[struct]!!) {
                val memberLayout = layout(member.type, structMemberMap)
                memberSizesInBytes.add(memberLayout.sizeInBytes)
                alignment = maxOf(alignment, memberLayout.alignmentInBytes)
            }
            return MemoryLayout(memberSizesInBytes, alignment)
        }

        internal fun layout(
            type: TypeSymbol,
            structMemberMap: Map<StructSymbol, Set<StructMemberSymbol>>,
        ): MemoryLayout {
            return when (type) {

                is TypeSymbol.Struct -> layoutStruct(structMemberMap.keys.first {
                    it.name == type.name
                }, structMemberMap)

                else -> MemoryLayout(listOf(4), 4)
            }
        }

        const val pointerSize = 4

        fun isStoredOnHeap(type: TypeSymbol): Boolean {
            return when (type) {
                is TypeSymbol.Struct -> true
                is TypeSymbol.String -> true
                else -> false
            }
        }
    }
}
