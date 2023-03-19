use std::str::FromStr;

use enum_index::IndexEnum;

use crate::ffi::FFIFunction;
use crate::instructions::instruction::Instruction;
use crate::instructions::OpCode;
use crate::preprocessor::symbol_table::SymbolTable;
use crate::utils::{decode_signed, decode_string, decode_unsigned, encode_signed, encode_string, encode_unsigned};

pub struct ByteCodeTranslator<'a> {
    content: String,
    symbol_table: &'a SymbolTable,
}

impl<'a> ByteCodeTranslator<'a> {
    pub fn new(content: &str, symbol_table: &'a SymbolTable) -> Self {
        Self {
            content: content.to_string(),
            symbol_table,
        }
    }
    pub fn translate(&self) -> Vec<u8> {
        let mut byte_code: Vec<u8> = Vec::new();
        let mut split = self.content.split_whitespace();
        while let Some(s) = split.next() {
            let op_code = self.parse_number(s).unwrap() as u32;
            let operand = split.next().unwrap();
            let operand = match self.parse_number(operand) {
                None => {
                    if op_code == OpCode::FFCALL as u32 {
                        let value = FFIFunction::get_index(operand).expect(format!("Unknown FFI function: {}", operand).as_str());
                        value as i32
                    } else {
                        let symbol = self.symbol_table.get_symbol(operand).expect(format!("Unknown symbol: {}", operand).as_str());
                        *symbol as i32
                    }
                }
                Some(operand) => { operand }
            };
            let offset = match self.parse_number(split.next().unwrap()) {
                None => panic!("Invalid offset"),
                Some(operand) => { operand as u32 }
            };

            byte_code.extend_from_slice(&encode_unsigned(op_code));

            byte_code.extend_from_slice(&encode_signed(operand));

            byte_code.extend_from_slice(&encode_unsigned(offset));
        }
        byte_code
    }

    fn parse_number(&self, s: &str) -> Option<i32> {
        if s.starts_with("0b") {
            i32::from_str_radix(&s[2..], 2).ok()
        } else if s.starts_with("0x") {
            i32::from_str_radix(&s[2..], 16).ok()
        } else {
            i32::from_str(s).ok()
        }
    }
}

pub struct ByteCodeParser<'a> {
    byte_code: &'a [u8],
    pub instruction_counter: usize,
}

impl<'a> ByteCodeParser<'a> {
    pub fn new(byte_code: &'a [u8]) -> Self {
        Self {
            byte_code,
            instruction_counter: 0,
        }
    }

    const fn get_instruction_size() -> usize {
        12
    }

    pub fn parse_instruction(&mut self) -> Result<Option<Instruction>, String> {
        let index = self.instruction_counter * Self::get_instruction_size();
        let op_code = decode_unsigned(index, self.byte_code).map_err(|_| "Reached end of programm while parsing. This means that there is either no halt or no return in a function.")?;
        let op_code = unsafe { OpCode::from_op_code(op_code) };
        let operand = decode_signed(index + 4, self.byte_code).unwrap_or(0);
        let offset = decode_unsigned(index + 8, self.byte_code).unwrap_or(4);

        self.go_to(self.instruction_counter + 1);
        Ok(Some(Instruction {
            opcode: op_code,
            operand,
            offset,
        }))
    }


    pub fn go_to(&mut self, instruction_count: usize) {
        self.instruction_counter = instruction_count;
    }
}

