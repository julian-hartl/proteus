package lang.proteus.emit

import lang.proteus.symbols.FunctionSymbol
import lang.proteus.symbols.StructMemberSymbol
import lang.proteus.symbols.StructSymbol
import lang.proteus.symbols.TypeSymbol

internal class ProteusByteCodeApi(private val structMemberMap: Map<StructSymbol, Set<StructMemberSymbol>>) {
    private var _stackFrameSize: Int = 0

    val stackFrameSize: Int
        get() = _stackFrameSize

    fun alloc(bytes: Int): String {
        _stackFrameSize += bytes
        return "alloc $bytes"
    }

    fun push(value: Int): String {
        _stackFrameSize += MemoryLayout.layout(TypeSymbol.Int, structMemberMap).sizeInBytes
        return "push $value"
    }

    fun pushb(value: Int): String {
        _stackFrameSize++
        return "pushb $value"
    }

    fun store(offset: Int, bytes: Int): String {
        _stackFrameSize -= bytes
        return "store $offset ($bytes)"
    }

    fun loada(offset: Int): String {
        _stackFrameSize += MemoryLayout.pointerSize
        return "loada $offset"
    }

    fun rload(offset: Int, bytes: Int): String {
        _stackFrameSize -= MemoryLayout.pointerSize
        _stackFrameSize += bytes
        return "rload $offset ($bytes)"
    }

    fun load(offset: Int, bytes: Int): String {
        _stackFrameSize += bytes
        return "load $offset ($bytes)"
    }

    fun halt(): String {
        return "halt"
    }

    fun itoa(): String {
        return "itoa"
    }

    fun ffcall(function: FunctionSymbol): String {
        val argSize = function.parameters.sumOf { MemoryLayout.layout(it.type, structMemberMap).sizeInBytes }
        _stackFrameSize -= argSize
        _stackFrameSize += MemoryLayout.layout(function.returnType, structMemberMap).sizeInBytes
        return "ffcall ${function.simpleName}"
    }

    fun call(function: FunctionSymbol): String {
        val argSize = function.parameters.sumOf { MemoryLayout.layout(it.type, structMemberMap).sizeInBytes }
        _stackFrameSize -= argSize
        _stackFrameSize += MemoryLayout.layout(function.returnType, structMemberMap).sizeInBytes
        return "call ${function.simpleName}"
    }

    fun iadd(): String {
        _stackFrameSize -= MemoryLayout.layout(TypeSymbol.Int, structMemberMap).sizeInBytes
        return "iadd"
    }

    fun isub(): String {
        _stackFrameSize -= MemoryLayout.layout(TypeSymbol.Int, structMemberMap).sizeInBytes
        return "isub"
    }

    fun imul(): String {
        _stackFrameSize -= MemoryLayout.layout(TypeSymbol.Int, structMemberMap).sizeInBytes
        return "imul"
    }

    fun idiv(): String {
        _stackFrameSize -= MemoryLayout.layout(TypeSymbol.Int, structMemberMap).sizeInBytes
        return "idiv"
    }

    fun imod(): String {
        _stackFrameSize -= MemoryLayout.layout(TypeSymbol.Int, structMemberMap).sizeInBytes
        return "imod"
    }

    fun iand(): String {
        _stackFrameSize -= MemoryLayout.layout(TypeSymbol.Int, structMemberMap).sizeInBytes
        return "iand"
    }

    fun ior(): String {
        _stackFrameSize -= MemoryLayout.layout(TypeSymbol.Int, structMemberMap).sizeInBytes
        return "ior"
    }

    fun ixor(): String {
        _stackFrameSize -= MemoryLayout.layout(TypeSymbol.Int, structMemberMap).sizeInBytes
        return "ixor"
    }

    fun inot(): String {
        return "inot"
    }

    fun ieq(): String {
        _stackFrameSize -= MemoryLayout.layout(TypeSymbol.Int, structMemberMap).sizeInBytes
        return "ieq"
    }

    fun ine(): String {
        _stackFrameSize -= MemoryLayout.layout(TypeSymbol.Int, structMemberMap).sizeInBytes
        return "ine"
    }

    fun ilt(): String {
        _stackFrameSize -= MemoryLayout.layout(TypeSymbol.Int, structMemberMap).sizeInBytes
        return "ilt"
    }

    fun ile(): String {
        _stackFrameSize -= MemoryLayout.layout(TypeSymbol.Int, structMemberMap).sizeInBytes
        return "ile"
    }

    fun igt(): String {
        _stackFrameSize -= MemoryLayout.layout(TypeSymbol.Int, structMemberMap).sizeInBytes
        return "igt"
    }

    fun ige(): String {
        _stackFrameSize -= MemoryLayout.layout(TypeSymbol.Int, structMemberMap).sizeInBytes
        return "ige"
    }

    fun ineg(): String {
        return "ineg"
    }


    fun beginStackFrame(function: FunctionSymbol): String {
        _stackFrameSize = 0
        return "${function.simpleName}:"
    }

    fun iret(
        bytes: Int,
    ): String {
        _stackFrameSize = 0
        return "iret $bytes"
    }

    fun jz(name: String): String {
        _stackFrameSize -= MemoryLayout.layout(TypeSymbol.Int, structMemberMap).sizeInBytes
        return "jz $name"
    }

    fun jmp(name: String): String {
        return "jmp $name"
    }

    fun nop(): String {
        return "nop"
    }

    fun halloc(type: TypeSymbol): String {
        val size = MemoryLayout.layout(type, structMemberMap).sizeInBytes
        _stackFrameSize += MemoryLayout.pointerSize
        return "halloc $size"
    }

    fun dhalloc(): String {
        return "dhalloc"
    }

    fun rstore(type: TypeSymbol): String {
        val size = MemoryLayout.layout(type, structMemberMap).sizeInBytes
        _stackFrameSize -= size
        return rstore(0, size)
    }

    fun rstore(offset: Int, bytes: Int): String {
        _stackFrameSize -= MemoryLayout.pointerSize
        return "rstore $offset ($bytes)"
    }

    fun pushsp(offset: Int): String {
        _stackFrameSize += MemoryLayout.pointerSize
        return "pushsp $offset"
    }

    fun free(type: TypeSymbol): String {
        val size = MemoryLayout.layout(type.deref(), structMemberMap).sizeInBytes
        return "free $size"
    }

    fun btoa(): String{
        return "btoa"
    }
}