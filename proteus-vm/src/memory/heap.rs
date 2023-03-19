#[derive(Debug)]
pub struct Heap {
    pub memory: Vec<u8>,
    pub free_list: Vec<FreeBlock>,
}

#[derive(Debug)]
pub struct FreeBlock {
    pub start: usize,
    pub size: usize,
}

impl Heap {
    pub fn new(size: usize) -> Self {
        Self {
            memory: vec![0; size],
            free_list: vec![FreeBlock { start: 0, size }],
        }
    }

    pub fn allocate(&mut self, size: usize) -> Result<usize, String> {
        let mut best_fit: Option<usize> = None;
        for (index, block) in self.free_list.iter().enumerate() {
            if block.size >= size {
                match best_fit {
                    Some(best_fit_index) => {
                        if self.free_list[best_fit_index].size > block.size {
                            best_fit = Some(index);
                        }
                    }
                    None => best_fit = Some(index),
                }
            }
        }
        match best_fit {
            Some(index) => {
                let block = self.free_list.remove(index);
                if block.size > size {
                    self.free_list.push(FreeBlock {
                        start: block.start + size,
                        size: block.size - size,
                    });
                }
                Ok(block.start)
            }
            None => Err("Out of memory".to_string()),
        }
    }

    pub fn free(&mut self, start: usize, size: usize) -> Result<(), String> {
        if start + size > self.memory.len() {
            return Err(format!("Cannot free memory at {} with size {}", start, size));
        }
        self.free_list.push(FreeBlock { start, size });
        self.free_list.sort_by(|a, b| a.start.cmp(&b.start));
        let mut index = 0;
        while index < self.free_list.len() - 1 {
            let current = &self.free_list[index];
            let next = &self.free_list[index + 1];
            if current.start + current.size == next.start {
                self.free_list[index].size += next.size;
                self.free_list.remove(index + 1);
            } else {
                index += 1;
            }
        }
        Ok(())
    }

    pub fn load(&self, start: usize, size: usize) -> Result<&[u8], String> {
        if start + size > self.memory.len() {
            return Err("Out of bounds".to_string());
        }
        Ok(&self.memory[start..start + size])
    }


    pub fn store(&mut self, start: usize, data: &[u8]) -> Result<(), String> {
        if start + data.len() > self.memory.len() {
            return Err("Out of bounds".to_string());
        }
        // check if is in free list
        for block in self.free_list.iter() {
            if start >= block.start && start + data.len() <= block.start + block.size {
                return Err("Cannot store in free memory".to_string());
            }
        }
        self.memory[start..start + data.len()].copy_from_slice(data);
        Ok(())
    }



    pub fn allocate_string(&mut self, string: &str) -> Result<usize, String> {
        let mut data = string.as_bytes().to_vec();
        data.push(0);
        let start = self.allocate(data.len())?;
        self.store(start, &data)?;
        Ok(start)
    }

    pub fn print_state(&self) {
        println!("Free list: {:?}", self.free_list);
    }
}