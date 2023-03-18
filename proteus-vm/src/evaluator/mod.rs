use std::error::Error;

use crate::ffi::{FFIFunction, FFIType, FFIValue};
use crate::instructions::instruction::Instruction;
use crate::instructions::OpCode;
use crate::loading::ByteCodeParser;
use crate::memory::heap::Heap;
use crate::memory::Memory;
use crate::utils::{decode_signed, decode_unsigned, encode_signed, encode_unsigned};

pub fn evaluate(byte_code: &[u8]) -> Result<(), Box<dyn Error>> {
    let mut evaluator = Evaluator::new(byte_code);
    evaluator.evaluate()
}

pub struct Evaluator<'a> {
    halt: bool,
    pub byte_code_parser: ByteCodeParser<'a>,
    stack_frames: Vec<u32>,
    pub memory: Memory,
}

impl<'a> Evaluator<'a> {
    pub(crate) fn print_state(&self) {
        println!(
            "Current stack frame offset: {}",
            self.stack_frames.last().unwrap_or(&0)
        );
        println!("Stack:\n{}", self.memory.stack_frame());
        println!(
            "Current instruction: {:?}",
            self.byte_code_parser.instruction_counter
        );
        self.memory.heap.print_state();
    }
}

impl<'a> Iterator for Evaluator<'a> {
    type Item = Instruction;

    fn next(&mut self) -> Option<Self::Item> {
        if self.halt {
            None
        } else {
            let instruction = self.byte_code_parser.parse_instruction();
            let instruction = match instruction {
                Ok(instruction) => instruction,
                Err(e) => {
                    println!("{}", e);
                    return None;
                }
            };
            instruction
        }
    }
}

const POINTER_SIZE: usize = 4;


impl<'a> Evaluator<'a> {
    pub fn new(instructions: &'a [u8]) -> Self {
        Self {
            halt: false,
            byte_code_parser: ByteCodeParser::new(instructions),
            stack_frames: vec![0],
            memory: Memory::new(),
        }
    }

    pub fn evaluate(&mut self) -> Result<(), Box<dyn Error>> {
        while let Some(instruction) = self.next() {
            self.evaluate_instruction(&instruction).map_err(
                |e| {
                    format!(
                        "Error while evaluating instruction {:?}: {}",
                        instruction, e
                    )
                }
            ).unwrap();
        }
        Ok(())
    }

    pub fn evaluate_instruction(
        &mut self,
        instruction: &Instruction,
    ) -> Result<(), Box<dyn Error>> {
        let operand = instruction.operand;
        let offset = instruction.offset;
        match instruction.opcode {
            OpCode::ALLOC => self.alloc(operand as u32),
            OpCode::FREE => self.free(operand as u32),
            OpCode::LOAD => self.load(operand, offset),
            OpCode::STORE => self.store(operand, offset),
            OpCode::PUSH => self.push(operand),
            OpCode::IADD => self.iadd(),
            OpCode::ISUB => self.isub(),
            OpCode::IMUL => self.imul(),
            OpCode::IDIV => self.idiv(),
            OpCode::IMOD => self.imod(),
            OpCode::IEQ => self.ieq(),
            OpCode::ILT => self.ilt(),
            OpCode::ILE => self.ilt(),
            OpCode::IAND => self.iadd(),
            OpCode::IOR => self.ior(),
            OpCode::IXOR => self.ixor(),
            OpCode::INOT => self.inot(),
            OpCode::JMP => self.jmp(operand as u32),
            OpCode::JZ => self.jz(operand as u32),
            OpCode::JNZ => self.jnz(operand as u32),
            OpCode::CALL => self.call(operand as u32),
            OpCode::HALT => {
                self.halt = true;
                Ok(())
            }
            OpCode::POP => self.pop(),
            OpCode::IRET => self.iret(operand as u32),
            OpCode::IGT => self.igt(),
            OpCode::IGE => self.ige(),
            OpCode::INE => self.ine(),
            OpCode::HALLOC => self.halloc(operand as u32),
            OpCode::FFCALL => self.ffcall(operand as u32),
            OpCode::SADD => self.sadd(),
            OpCode::PUSHB => self.pushb(operand as u8),
            OpCode::ITOA => self.itoa(),
            OpCode::NOP => Ok(()),
            OpCode::STOREB => self.storeb(operand),
            OpCode::LOADA => self.loada(operand),
            OpCode::PUSHSP => self.pushsp(operand),
            OpCode::RLOAD => self.rload(operand, offset),
            OpCode::RSTORE => self.rstore(operand, offset),
            OpCode::DHALLOC => self.dhalloc()
        }
    }

    fn pushsp(&mut self, offset: i32) -> Result<(), Box<dyn Error>> {
        let sp = self.memory.stack_pointer as i32 + offset;
        self.push(sp)?;
        Ok(())
    }

    /// Add the given number of elements to the stack
    fn alloc(&mut self, bytes: u32) -> Result<(), Box<dyn Error>> {
        if self.stack_frames.len() < 1 {
            return Err(Box::new(std::io::Error::new(std::io::ErrorKind::Other, "Alloc called without a stack frame. This most likely means that you are trying to allocate memory outside of a function. Use halloc instead.")));
        }
        self.memory.move_stack_pointer_by(bytes as usize);
        Ok(())
    }


    fn free(&mut self, index: u32) -> Result<(), Box<dyn Error>> {
        let ptr = self.remove_top()?;
        self.memory.free(ptr as usize, index as usize)?;
        Ok(())
    }


    /// Loads the address of the element at the given index
    fn loada(&mut self, index: i32) -> Result<(), Box<dyn Error>> {
        let index = *self.stack_frames.last().unwrap() as i32 + index;
        self.push(index)?;
        Ok(())
    }


    /// Add the top two elements on the stack
    fn iadd(&mut self) -> Result<(), Box<dyn Error>> {
        let a = self.remove_top()?;
        let b = self.remove_top()?;
        let result = a + b;
        self.push(result)?;
        Ok(())
    }

    fn sadd(&mut self) -> Result<(), Box<dyn Error>> {
        let mut s: Vec<u8> = vec![];

        while let Ok(byte) = self.memory.pop(1) {
            let byte = byte[0];
            if byte == 0 {
                break;
            }
            s.push(byte);
        }

        while let Ok(byte) = self.memory.pop(1) {
            let byte = byte[0];
            s.push(byte);
            if byte == 0 {
                break;
            }
        }

        println!("sadd: {:?}", s);
        // print bytes as string
        self.memory.push(&s)?;
        Ok(())
    }

    fn push(&mut self, value: i32) -> Result<(), Box<dyn Error>> {
        self.memory.push(&encode_signed(value))?;
        Ok(())
    }

    fn pushb(&mut self, value: u8) -> Result<(), Box<dyn Error>> {
        self.memory.push(&[value])?;
        Ok(())
    }


    fn isub(&mut self) -> Result<(), Box<dyn Error>> {
        let a = self.remove_top()?;
        let b = self.remove_top()?;
        let result = a - b;
        self.memory.push(&encode_signed(result))?;
        Ok(())
    }

    pub fn remove_top(&mut self) -> Result<i32, String> {
        decode_signed(0, &self.memory.pop(POINTER_SIZE)?)
    }

    pub fn remove_top_byte(&mut self) -> Result<u8, String> {
        let result = self.memory.pop(1)?[0];
        Ok(result)
    }

    pub fn remove_top_bytes(&mut self, bytes: u32) -> Result<Vec<u8>, String> {
        let result = self.memory.pop(bytes as usize)?;
        Ok(result.to_vec())
    }

    fn read_top(&self) -> Result<i32, String> {
        let result = decode_signed(0, &self.memory.peek(POINTER_SIZE)?)?;
        Ok(result)
    }
    fn imul(&mut self) -> Result<(), Box<dyn Error>> {
        let a = self.remove_top()?;
        let b = self.remove_top()?;
        let result = a * b;
        self.push(result)?;
        Ok(())
    }
    fn idiv(&mut self) -> Result<(), Box<dyn Error>> {
        let a = self.remove_top()?;
        let b = self.remove_top()?;
        let result = a / b;
        self.push(result)?;
        Ok(())
    }
    fn imod(&mut self) -> Result<(), Box<dyn Error>> {
        let a = self.remove_top()?;
        let b = self.remove_top()?;
        let result = a % b;
        self.push(result)?;

        Ok(())
    }
    fn ieq(&mut self) -> Result<(), Box<dyn Error>> {
        let a = self.remove_top()?;
        let b = self.remove_top()?;
        let result = (a == b) as i32;
        self.push(result)?;

        Ok(())
    }
    fn ine(&mut self) -> Result<(), Box<dyn Error>> {
        let a = self.remove_top()?;
        let b = self.remove_top()?;
        let result = (a != b) as i32;
        self.push(result)?;

        Ok(())
    }
    fn ilt(&mut self) -> Result<(), Box<dyn Error>> {
        let a = self.remove_top()?;
        let b = self.remove_top()?;
        let result = (a < b) as i32;
        self.push(result)?;

        Ok(())
    }

    fn igt(&mut self) -> Result<(), Box<dyn Error>> {
        let a = self.remove_top()?;
        let b = self.remove_top()?;
        let result = (a > b) as i32;
        self.push(result)?;

        Ok(())
    }

    fn ige(&mut self) -> Result<(), Box<dyn Error>> {
        let a = self.remove_top()?;
        let b = self.remove_top()?;
        let result = (a >= b) as i32;
        self.push(result)?;

        Ok(())
    }

    fn ior(&mut self) -> Result<(), Box<dyn Error>> {
        let a = self.remove_top()?;
        let b = self.remove_top()?;
        let result = a | b;
        self.push(result)?;


        Ok(())
    }
    fn ixor(&mut self) -> Result<(), Box<dyn Error>> {
        let a = self.remove_top()?;
        let b = self.remove_top()?;
        let result = a ^ b;
        self.push(result)?;

        Ok(())
    }
    fn inot(&mut self) -> Result<(), Box<dyn Error>> {
        let a = self.remove_top()?;
        let result = !a;
        self.push(result)?;

        Ok(())
    }
    fn jmp(&mut self, dest: u32) -> Result<(), Box<dyn Error>> {
        self.byte_code_parser.go_to(dest as usize);
        Ok(())
    }
    fn jz(&mut self, dest: u32) -> Result<(), Box<dyn Error>> {
        let a = self.remove_top()?;
        if a == 0 {
            self.jmp(dest)?;
        }
        Ok(())
    }
    fn jnz(&mut self, dest: u32) -> Result<(), Box<dyn Error>> {
        let a = self.remove_top()?;
        if a != 0 {
            self.jmp(dest)?;
        }
        Ok(())
    }
    fn call(&mut self, dest: u32) -> Result<(), Box<dyn Error>> {
        let address = self.byte_code_parser.instruction_counter as u32;
        self.push(address as i32)?;
        self.stack_frames.push(self.memory.stack_pointer as u32);
        self.jmp(dest)?;
        Ok(())
    }
    fn pop(&mut self) -> Result<(), Box<dyn Error>> {
        self.remove_top()?;
        Ok(())
    }
    fn iret(&mut self, bytes: u32) -> Result<(), Box<dyn Error>> {
        // self.swap()?;
        let value = self.remove_top_bytes(bytes)?;
        self.clear_stack_frame();
        let dest = self.remove_top()?;
        self.memory.push(&value)?;
        self.jmp(dest as u32)
    }

    fn clear_stack_frame(&mut self) {
        let offset = self.stack_frames.pop();
        if let Some(offset) = offset {
            self.memory.move_stack_pointer_to(offset as usize)
        }
    }

    fn halloc(&mut self, bytes: u32) -> Result<(), Box<dyn Error>> {
        let pointer = self.memory.allocate_heap(bytes as usize)?;
        self.memory.push(
            &encode_unsigned(pointer as u32))?;
        Ok(())
    }

    fn dhalloc(&mut self) -> Result<(), Box<dyn Error>> {
        let bytes = self.remove_top()?;
        let pointer = self.memory.allocate_heap(bytes as usize)?;
        self.push(pointer as i32)?;
        Ok(())
    }

    fn load(&mut self, offset: i32, size: u32) -> Result<(), Box<dyn Error>> {
        let base_address = self.stack_frames.last().unwrap();
        let address: u32 = ((*base_address as i32) + offset) as u32;
        let memory = &mut self.memory;
        let result = memory.load(address as usize, size as usize)?.to_vec();
        memory.push(&result)?;
        Ok(())
    }

    fn rload(&mut self, offset: i32, size: u32) -> Result<(), Box<dyn Error>> {
        let base_address = self.remove_top()?;
        let address = base_address + offset;
        let memory = &mut self.memory;
        let result = memory.load(address as usize, size as usize)?.to_vec();
        memory.push(&result)?;
        Ok(())
    }

    fn store(&mut self, offset: i32, bytes: u32) -> Result<(), Box<dyn Error>> {
        let value = &self.remove_top_bytes(bytes)?;
        let base_address = self.stack_frames.last().unwrap();
        let address = (*base_address) as i32 + offset;
        self.memory.store(address as usize, &value)?;
        Ok(())
    }

    fn rstore(&mut self, offset: i32, bytes: u32) -> Result<(), Box<dyn Error>> {
        let value = &self.remove_top_bytes(bytes)?;
        let base_address = self.read_top()?;
        let address = base_address + offset;
        self.memory.store(address as usize, &value)?;
        Ok(())
    }

    fn storeb(&mut self, offset: i32) -> Result<(), Box<dyn Error>> {
        let value = self.remove_top_byte()?;
        let base_address = self.read_top()?;
        let address = base_address + offset;
        self.memory.store(address as usize, &[value])?;
        Ok(())
    }

    fn ffcall(&mut self, function_name: u32) -> Result<(), Box<dyn Error>> {
        let function = FFIFunction::find(&(function_name as usize))
            .ok_or(format!("Function {} not found", function_name))?;
        let mut args = Vec::new();
        for fArg in &function.arguments {
            let arg = match fArg {
                FFIType::I32 => FFIValue::I32(self.remove_top()?),
                FFIType::I64 => {
                    let arg = &self.remove_top()?;
                    let arg2 = &self.remove_top()?;
                    FFIValue::I64(((*arg as i64) << 32) | (*arg2 as i64))
                }
                FFIType::String => {
                    let address = self.remove_top()?;
                    let string = self.memory.get_string(address as usize)?;
                    self.memory.pop(string.len() + 1)?;
                    FFIValue::String(string)
                }
                FFIType::Void => FFIValue::Void,
            };
            args.push(arg);
        }
        let result = function.call(args)?;
        self.store_ffi_result(result)
    }

    fn store_ffi_result(&mut self, value: FFIValue) -> Result<(), Box<dyn Error>> {
        Ok(match value {
            FFIValue::I32(value) => self.push(value)?,
            FFIValue::I64(value) => {
                self.push((value >> 32) as i32)?;
                self.push((value & 0xFFFFFFFF) as i32)?;
            }

            FFIValue::String(value) => {
                unimplemented!("String return values are not implemented yet")
            }
            FFIValue::Void => {}
        })
    }

    // Converts an integer to a string
    fn itoa(&mut self) -> Result<(), Box<dyn Error>> {
        let value = self.remove_top()?;
        let string = value.to_string();
        let start = self.memory.stack_pointer;
        self.memory.push_string(&string)?;
        self.push(start as i32)?;
        Ok(())
    }
}
