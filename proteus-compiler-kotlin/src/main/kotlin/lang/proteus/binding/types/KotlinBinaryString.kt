package lang.proteus.binding.types

data class KotlinBinaryString(val binaryString: String) {
    override fun toString(): String {
        return "0b$binaryString"
    }
}