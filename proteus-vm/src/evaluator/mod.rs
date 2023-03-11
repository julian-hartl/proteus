use std::error::Error;
use std::sync::Mutex;

use crate::ffi::{FFIFunction, FFIType, FFIValue};
use crate::instructions::instruction::Instruction;
use crate::instructions::OpCode;
use crate::loading::ByteCodeParser;
use crate::memory::heap::Heap;
use crate::utils::{decode_signed, decode_string, decode_unsigned, encode_signed, encode_unsigned};

pub fn evaluate(byte_code: &[u8]) -> Result<(), Box<dyn Error>> {
    let mut evaluator = Evaluator::new(byte_code);
    evaluator.evaluate()
}

pub struct Evaluator<'a> {
    halt: bool,
    pub stack: Vec<u8>,
    byte_code_parser: ByteCodeParser<'a>,
    stack_frames: Vec<u32>,
    pub heap:  Heap,
}


impl<'a> Evaluator<'a> {
    pub(crate) fn print_state(&self) {
        // pretty print stack size, current stack, and current instruction
        println!("Current stack memory usage: {} byte", self.stack.len());
        println!("Current vm instructions memory usage: {} byte", self.byte_code_parser.instruction_counter * 8);
        println!("Current stack frame offset: {}", self.stack_frames.last().unwrap_or(&0));
        println!("Stack: {:?}", self.stack);
        println!("Current instruction: {:?}", self.byte_code_parser.instruction_counter);
        self.heap.print_state();
    }

    fn print_stack_as_vertical(&self) {
        for i in (0..self.stack.len()).rev() {
            println!("| {} |", self.stack[i]);
        }
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

const STACK_CELL_SIZE: usize = 4;

const HEAP_SIZE: usize = 1024 * 1024;

impl<'a> Evaluator<'a> {
    pub fn new(instructions: &'a [u8]) -> Self {
        Self {
            stack: Vec::new(),
            halt: false,
            byte_code_parser: ByteCodeParser::new(instructions),
            stack_frames: vec![0],
            heap: Heap::new(HEAP_SIZE),
        }
    }

    pub fn evaluate(&mut self) -> Result<(), Box<dyn Error>> {
        while let Some(instruction) = self.next() {
            self.evaluate_instruction(&instruction)?;
        }
        Ok(())
    }

    pub fn evaluate_instruction(&mut self, instruction: &Instruction) -> Result<(), Box<dyn Error>> {
        let operand = instruction.operand;
        let offset = instruction.offset;
        match instruction.opcode {
            OpCode::ALLOC => self.alloc(operand as u32),
            OpCode::FREE => self.free(operand as u32),
            OpCode::LOAD => self.load(operand),
            OpCode::STORE => self.store(operand as u32),
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
            OpCode::IRET => self.iret(),
            OpCode::IGT => self.igt(),
            OpCode::IGE => self.ige(),
            OpCode::INE => self.ine(),
            OpCode::HALLOC => self.halloc(operand as u32),
            OpCode::HLOAD => self.hload(operand, offset),
            OpCode::HSTORE => self.hstore(operand),
            OpCode::FFCALL => self.ffcall(operand as u32),
            OpCode::SADD => self.sadd(),
            OpCode::PUSHB => self.pushb(operand as u8),
            OpCode::HSTOREB => self.hstoreb(operand),
            OpCode::ITOA => self.itoa(),
        }
    }

    /// Add the given number of elements to the stack
    fn alloc(&mut self, size: u32) -> Result<(), Box<dyn Error>> {
        self.stack.resize(self.stack.len() + (size as usize) * STACK_CELL_SIZE, 0);
        Ok(())
    }

    /// Free the element at the given index
    fn free(&mut self, index: u32) -> Result<(), Box<dyn Error>> {
        self.stack.remove(index as usize);
        Ok(())
    }

    /// Load the element at the given index
    fn load(&mut self, index: i32) -> Result<(), Box<dyn Error>> {
        let index = *self.stack_frames.last().unwrap() as i32 + index * STACK_CELL_SIZE as i32;
        let slice = &self.stack[index as usize..index as usize + STACK_CELL_SIZE].to_vec();
        self.stack.extend_from_slice(slice);
        Ok(())
    }

    /// Store the element at the given index
    fn store(&mut self, index: u32) -> Result<(), Box<dyn Error>> {
        let index = self.stack_frames.last().unwrap() + index * STACK_CELL_SIZE as u32;
        let value = self.remove_top()?;
        if index as usize >= self.stack.len() {
            return Err(format!("Index {} exceeds stack size({})", index, self.stack.len()).into());
        }
        let encoded_value = encode_signed(value);
        self.stack.splice(index as usize..index as usize + STACK_CELL_SIZE, encoded_value.iter().cloned());
        Ok(())
    }

    /// Add the top two elements on the stack
    fn iadd(&mut self) -> Result<(), Box<dyn Error>> {
        let a = self.remove_top()?;
        let b = self.remove_top()?;
        let result = a + b;
        self.stack.extend_from_slice(&encode_signed(result));
        Ok(())
    }

    fn sadd(&mut self) -> Result<(), Box<dyn Error>> {
        let s_pointer_a = self.remove_top()?;
        let s_pointer_b = self.remove_top()?;
        let s_a = self.heap.get_string(s_pointer_a as usize)?;
        let s_b = self.heap.get_string(s_pointer_b as usize)?;
        let s_c = s_a + &s_b;
        let s_c_pointer = self.heap.allocate_string(&s_c)?;
        self.push(s_c_pointer as i32)?;
        Ok(())
    }


    fn push(&mut self, value: i32) -> Result<(), Box<dyn Error>> {
        self.stack.extend_from_slice(&encode_signed(value));
        Ok(())
    }

    fn pushb(&mut self, value: u8) -> Result<(), Box<dyn Error>> {
        self.stack.push(value);
        Ok(())
    }

    fn isub(&mut self) -> Result<(), Box<dyn Error>> {
        let a = self.remove_top()?;
        let b = self.remove_top()?;
        let result = a - b;
        self.stack.extend_from_slice(&encode_signed(result));
        Ok(())
    }

    pub fn remove_top(&mut self) -> Result<i32, String> {
        let result = i32::from_be_bytes(self.stack[self.stack.len() - STACK_CELL_SIZE..].try_into().unwrap());
        self.stack.truncate(self.stack.len() - STACK_CELL_SIZE);
        Ok(result)
    }

    pub fn remove_top_byte(&mut self) -> Result<u8, String> {
        let result = self.stack[self.stack.len() - 1];
        self.stack.truncate(self.stack.len() - 1);
        Ok(result)
    }

    fn read_top(&self) -> Result<i32, String> {
        let result = i32::from_be_bytes(self.stack[self.stack.len() - STACK_CELL_SIZE..].try_into().unwrap());
        Ok(result)
    }
    fn imul(&mut self) -> Result<(), Box<dyn Error>> {
        let a = self.remove_top()?;
        let b = self.remove_top()?;
        let result = a * b;
        self.stack.extend_from_slice(&encode_signed(result));
        Ok(())
    }
    fn idiv(&mut self) -> Result<(), Box<dyn Error>> {
        let a = self.remove_top()?;
        let b = self.remove_top()?;
        let result = a / b;
        self.stack.extend_from_slice(&encode_signed(result));
        Ok(())
    }
    fn imod(&mut self) -> Result<(), Box<dyn Error>> {
        let a = self.remove_top()?;
        let b = self.remove_top()?;
        let result = a % b;
        self.stack.extend_from_slice(&encode_signed(result));
        Ok(())
    }
    fn ieq(&mut self) -> Result<(), Box<dyn Error>> {
        let a = self.remove_top()?;
        let b = self.remove_top()?;
        let result = (a == b) as i32;
        self.stack.extend_from_slice(&encode_signed(result));
        Ok(())
    }
    fn ine(&mut self) -> Result<(), Box<dyn Error>> {
        let a = self.remove_top()?;
        let b = self.remove_top()?;
        let result = (a != b) as i32;
        self.stack.extend_from_slice(&encode_signed(result));
        Ok(())
    }
    fn ilt(&mut self) -> Result<(), Box<dyn Error>> {
        let a = self.remove_top()?;
        let b = self.remove_top()?;
        let result = (a < b) as i32;
        self.stack.extend_from_slice(&encode_signed(result));
        Ok(())
    }

    fn igt(&mut self) -> Result<(), Box<dyn Error>> {
        let a = self.remove_top()?;
        let b = self.remove_top()?;
        let result = (a > b) as i32;
        self.stack.extend_from_slice(&encode_signed(result));
        Ok(())
    }

    fn ige(&mut self) -> Result<(), Box<dyn Error>> {
        let a = self.remove_top()?;
        let b = self.remove_top()?;
        let result = (a >= b) as i32;
        self.stack.extend_from_slice(&encode_signed(result));
        Ok(())
    }

    fn ior(&mut self) -> Result<(), Box<dyn Error>> {
        let a = self.remove_top()?;
        let b = self.remove_top()?;
        let result = a | b;
        self.stack.extend_from_slice(&encode_signed(result));

        Ok(())
    }
    fn ixor(&mut self) -> Result<(), Box<dyn Error>> {
        let a = self.remove_top()?;
        let b = self.remove_top()?;
        let result = a ^ b;
        self.stack.extend_from_slice(&encode_signed(result));
        Ok(())
    }
    fn inot(&mut self) -> Result<(), Box<dyn Error>> {
        let a = self.remove_top()?;
        let result = !a;
        self.stack.extend_from_slice(&encode_signed(result));
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
        self.stack_frames.push(self.stack.len() as u32);
        self.jmp(dest)?;
        Ok(())
    }
    fn pop(&mut self) -> Result<(), Box<dyn Error>> {
        self.remove_top()?;
        Ok(())
    }
    fn iret(&mut self) -> Result<(), Box<dyn Error>> {
        // self.swap()?;
        let value = self.remove_top()?;
        self.clear_stack_frame();
        let dest = self.remove_top()?;
        self.push(value)?;
        self.jmp(dest as u32)
    }

    fn clear_stack_frame(&mut self) {
        let offset = self.stack_frames.pop();
        if let Some(offset) = offset {
            self.stack.truncate(offset as usize);
        }
    }
    fn swap(&mut self) -> Result<(), Box<dyn Error>> {
        let top = self.stack.last().ok_or("Stack is empty")?.clone();
        let len = self.stack.len();
        let second = self.stack.get(len - 2).ok_or("Stack is empty")?.clone();
        self.stack[len - 1] = second;
        self.stack[len - 2] = top;

        Ok(())
    }
    fn halloc(&mut self, bytes: u32) -> Result<(), Box<dyn Error>> {
        let heap = &mut self.heap;
        let pointer = heap.allocate(bytes as usize)?;
        self.stack.extend_from_slice(&encode_unsigned(pointer as u32));
        Ok(())
    }

    fn hload(&mut self, offset: i32, size: u32) -> Result<(), Box<dyn Error>> {
        let base_address = self.remove_top()?;
        let heap = &self.heap;
        let address = base_address + offset;
        let result = heap.load(address as usize, size as usize)?;
        self.stack.extend_from_slice(&result);
        Ok(())
    }

    fn hstore(&mut self, offset: i32) -> Result<(), Box<dyn Error>> {
        let value = self.remove_top()?;
        let base_address = self.read_top()?;
        let heap = &mut self.heap;
        let address = base_address + offset;
        heap.store(address as usize, &encode_signed(value))?;
        Ok(())
    }
    fn hstoreb(&mut self, offset: i32) -> Result<(), Box<dyn Error>> {
        let value = self.remove_top_byte()?;
        let base_address = self.read_top()?;
        let heap = &mut self.heap;
        let address = base_address + offset;
        heap.store(address as usize, &[value])?;
        Ok(())
    }

    fn ffcall(&mut self, function_name: u32) -> Result<(), Box<dyn Error>> {
        let function = FFIFunction::find(&(function_name as usize))
            .ok_or(format!("Function {} not found", function_name))?;
        let mut args = Vec::new();
        for fArg in &function.arguments {
            let arg = &self.remove_top()?;
            let arg = match fArg {
                FFIType::I32 => {
                    FFIValue::I32(*arg)
                }
                FFIType::I64 => {
                    let arg2 = &self.remove_top()?;
                    FFIValue::I64(((*arg as i64) << 32) | (*arg2 as i64))
                }
                FFIType::String => {
                    let pointer = arg;
                    let string = self.heap.get_string((*pointer) as usize)?.clone();
                    FFIValue::String(string)
                }
                FFIType::Void => {
                    FFIValue::Void
                }
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
                self.heap.allocate_string(&value)?;
            }
            FFIValue::Void => {
                self.push(0)?; // TODO: void is not 0
            }
        })
    }

    // Converts an integer to a string
    fn itoa(&mut self) -> Result<(), Box<dyn Error>> {
        let value = self.remove_top()?;
        let string = value.to_string();

        let pointer = self.heap.allocate_string(&string)?;
        self.push(pointer as i32)?;
        Ok(())
    }
}
