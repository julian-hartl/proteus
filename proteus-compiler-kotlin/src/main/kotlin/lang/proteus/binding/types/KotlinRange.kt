package lang.proteus.binding.types

data class KotlinRange(val lowerBound: Int, val upperBound: Int) {
    operator fun iterator(): Iterator<Int> {
        return object : Iterator<Int> {
            var current = lowerBound
            override fun hasNext(): Boolean {
                return current <= upperBound
            }

            override fun next(): Int {
                return current++
            }
        }
    }
}