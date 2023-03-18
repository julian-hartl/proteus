use std::mem::transmute;

pub mod instruction;

#[derive(EnumIndex, IndexEnum, Debug, EnumString)]
#[repr(u32)]
pub enum OpCode {
    NOP = 0x00,
    LOAD = 0x01,
    STORE = 0x02,
    ALLOC = 0x03,
    FREE = 0x04,
    PUSH = 0x05,
    POP = 0x06,
    JMP = 0x07,
    JZ = 0x08,
    JNZ = 0x09,
    CALL = 0x0A,
    IRET = 0x0B,
    IADD = 0x10,
    ISUB = 0x11,
    IMUL = 0x12,
    IDIV = 0x13,
    IMOD = 0x14,
    IEQ = 0x15,
    ILT = 0x16,
    ILE = 0x17,
    IGT = 0x18,
    IGE = 0x19,
    IAND = 0x20,
    IOR = 0x21,
    IXOR = 0x22,
    INOT = 0x23,
    INE = 0x24,
    SADD = 0x30,
    PUSHB = 0x40,
    STOREB = 0x48,
    LOADA = 0x60,
    RLOAD = 0x61,
    PUSHSP = 0x70,
    HALLOC = 0x80,
    FFCALL = 0x90,
    ITOA = 0x91,
    HALT = 0xFF,
}


impl OpCode {

    pub unsafe fn from_op_code(op_code: u32) -> OpCode {
        transmute(op_code)
    }

}










