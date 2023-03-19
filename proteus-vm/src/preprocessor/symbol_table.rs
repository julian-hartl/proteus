use std::collections::HashMap;

#[derive(Debug)]
pub struct SymbolTable {
    symbols: HashMap<String, u32>,
}

impl Default for SymbolTable {
    fn default() -> Self {
        Self {
            symbols: HashMap::new(),
        }
    }
}

impl SymbolTable {
    pub fn new() -> Self {
        Self::default()
    }

    pub fn add_symbol(&mut self, symbol: String, value: u32) {
        self.symbols.insert(symbol, value);
    }

    pub fn get_symbol(&self, symbol: &str) -> Option<&u32> {
        self.symbols.get(symbol)
    }
}