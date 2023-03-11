use crate::instructions::OpCode;

#[derive(Debug)]
pub struct Instruction {
    pub opcode: OpCode,
    pub operand: i32,
    pub offset: u32,
}

impl Instruction {
    pub fn new(opcode: OpCode, operand: i32, offset: u32) -> Self {
        Self { opcode, operand, offset }
    }
}