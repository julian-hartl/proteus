use std::error::Error;

pub mod heap;

#[repr(C)]
pub struct Memory {
    pub heap: heap::Heap,
    pub stack: Vec<u8>,
    pub stack_pointer: usize,
}

const HEAP_SIZE: usize = 1024 * 1024;
const STACK_SIZE: usize = 8096;

impl Memory {
    pub fn new() -> Self {
        Self {
            heap: heap::Heap::new(
                HEAP_SIZE
            ),
            stack: vec![0; STACK_SIZE],
            stack_pointer: 0,
        }
    }

    pub fn load(&self, address: usize, size: usize) -> Result<&[u8], String> {
        if self.is_heap_address(address) {
            let heap_address = address - self.heap_start();
            self.heap.load(heap_address, size)
        } else {
            self.stack_load(address, size)
        }
    }

    fn stack_load(&self, address: usize, size: usize) -> Result<&[u8], String> {
        if address + size > self.stack.len() {
            Err(format!("SIGSEV: {} + {} > {}", address, size, self.stack_pointer))
        } else {
            Ok(&self.stack[address..address + size])
        }
    }

    pub fn store(&mut self, address: usize, value: &[u8]) -> Result<(), String> {
        if self.is_heap_address(address) {
            let heap_address = address - self.heap_start();
            self.heap.store(heap_address, value)
        } else {
            self.stack_store(address, value)
        }
    }

    fn stack_store(&mut self, address: usize, value: &[u8]) -> Result<(), String> {
        if address + value.len() > self.stack.len() {
            Err(format!("Stack overflow: {} + {} > {}", address, value.len(), self.stack.len()))
        } else {
            self.stack[address..address + value.len()].copy_from_slice(value);
            Ok(())
        }
    }

    pub fn allocate_heap(&mut self, size: usize) -> Result<usize, String> {
        self.heap.allocate(size).map(
            |address| {
                address + self.heap_start()
            }
        )
    }

    pub fn move_stack_pointer_by(&mut self, offset: usize) {
        self.stack_pointer += offset;
    }

    pub fn move_stack_pointer_to(&mut self, address: usize) {
        if address > self.stack.len() {
            panic!("Stack overflow: {} > {}", address, self.stack.len());
        }
        self.stack_pointer = address;
    }

    pub fn push(&mut self, value: &[u8]) -> Result<usize, String> {
        let address = self.stack_pointer;
        self.stack_pointer += value.len();
        self.stack_store(address, value)?;
        Ok(address)
    }

    pub fn push_string(&mut self, value: &str) -> Result<(), Box<dyn Error>> {
        for byte in value.bytes() {
            self.push(&[byte])?;
        }
        self.push(&[0])?;
        Ok(())
    }

    pub fn pop(&mut self, size: usize) -> Result<&[u8], String> {
        self.stack_pointer -= size;
        let address = self.stack_pointer;
        let value = self.stack_load(address, size)?;
        Ok(value)
    }

    pub fn pop_string(&mut self) -> Result<String, String> {
        let mut string = String::new();
        let mut byte = self.pop(1)?[0];
        while byte != 0 {
            string.push(byte as char);
            byte = self.pop(1)?[0];
        }
        Ok(string)
    }

    pub fn peek(&self, size: usize) -> Result<&[u8], String> {
        self.stack_load(self.stack_pointer - size, size)
    }

    pub fn peek_down(&self, offset: usize, size: usize) -> Result<&[u8], String> {
        self.stack_load(self.stack_pointer - offset - size, size)
    }


    fn is_heap_address(&self, address: usize) -> bool {
        address >= self.stack.len()
    }

    /// Prints the stack frame in the following format:
    /// 0x00
    /// 0x10
    /// 0x20
    /// ...
    /// 0x100 <--- stack pointer
    /// 0x110
    ///
    /// It prints all the bytes in the stack, and highlights the stack pointer.
    /// It also prints 10 bytes beyond the stack pointer.
    pub fn stack_frame(&self) -> String {
        let mut frame = String::new();
        let mut i = 0;
        while i < self.stack_pointer + 10 {
            let value = self.stack.get(i).unwrap_or(&0);
            frame.push_str(&format!("0x{:02x}: ", i));
            if i == self.stack_pointer {
                frame.push_str(&format!("0x{:02x} <--- stack pointer)", value));
            } else {
                frame.push_str(&format!("0x{:02x}", value));
            }
            frame.push('\n');
            i += 1;
        }
        frame
    }

    pub fn get_string(&self, start: usize) -> Result<String, String> {
        let mut string = String::new();
        let mut index = if self.is_heap_address(start) {
            start - self.heap_start()
        } else {
            start
        };
        let memory = if self.is_heap_address(start) {
            &self.heap.memory
        } else {
            &self.stack
        };
        while memory[index] != 0 {
            string.push(memory[index] as char);
            index += 1;
        }
        Ok(string)
    }

    pub fn heap_start(&self) -> usize {
        self.stack.len()
    }
}