use std::str::FromStr;

use enum_index::EnumIndex;

use crate::instructions::OpCode;
use crate::preprocessor::symbol_table::SymbolTable;

pub mod symbol_table;

/// Replaces op codes with their corresponding assembly binary instructions
pub fn process(content: &str) -> (String, SymbolTable) {
    let mut pre_processor = PreProcessor::new(content);
    let transpiled = pre_processor.process();
    (transpiled, pre_processor.symbol_table)
}

struct PreProcessor {
    symbol_table: SymbolTable,
    content: String,
}

impl Default for PreProcessor {
    fn default() -> Self {
        Self {
            symbol_table: SymbolTable::new(),
            content: String::new(),
        }
    }
}

impl PreProcessor {
    pub fn new(content: &str) -> Self {
        Self {
            symbol_table: SymbolTable::new(),
            content: content.to_string(),
        }
    }

    fn process(&mut self) -> String {
        self.remove_blank_lines();
        let lines = self.content.lines();
        let mut transpiled = String::new();

        for (index, line) in lines.enumerate() {
            if index > 0 {
                transpiled.push_str("
            ");
            }
            let (line, label) = &self.process_line(line);
            transpiled.push_str(line);
            for label in label {
                self.symbol_table.add_symbol(label.clone(), index as u32);
            }
        }
        transpiled
    }

    fn remove_blank_lines(&mut self) {
        let lines = self.content.lines();
        let mut new_content = String::new();
        for line in lines {
            if !line.is_empty() {
                new_content.push_str(line);
                new_content.push_str("
");
            }
        }
        self.content = new_content;
    }

    fn process_line(&self, line: &str) -> (String, Vec<String>) {
        let tokens = self.tokenize_line(line);
        println!("Tokens: {:?}", tokens);
        if tokens.is_empty() {
            return (String::new(), vec![]);
        }
        let mut labels = vec![];
        let mut current: usize = 0;
        while let Some(label) = self.parse_label(&tokens[current..]) {
            labels.push(label);
            current += 2;
        }

        let op_code = if let Token::Identifier(name) = &tokens[current] {
            name
        } else {
            panic!("Invalid opcode");
        };
        let op_code: OpCode = OpCode::from_str(&op_code.to_uppercase()).expect(&*format!("Invalid opcode: {}", op_code));

        let operand = &tokens.get(current + 1);

        let offset = &tokens.get(current + 2);

        let mut transpiled = String::new();

        transpiled.push_str(&(op_code as u32).to_string());


        let value = match operand {
            None => {
                String::from("0")
            }
            Some(operand) => {
                match operand {
                    Token::Number(number) => {
                        number.to_string()
                    }
                    Token::Identifier(identifier) => {
                        identifier.clone()
                    }
                    _ => {
                        panic!("Invalid operand: {:?}", operand);
                    }
                }
            }
        };

        let offset = match offset {
            None => None,
            Some(offset) => {
                match offset {
                    Token::OpenParen => {
                        match &tokens.get(current + 3) {
                            None => panic!("Invalid offset"),
                            Some(offset) => {
                                let value = match offset {
                                    Token::Number(number) => {
                                        Some(number.to_string())
                                    }
                                    _ => {
                                        panic!("Invalid offset: {:?}", offset);
                                    }
                                };
                                match &tokens.get(current + 4) {
                                    None => panic!("Invalid offset"),
                                    Some(offset) => {
                                        match offset {
                                            Token::CloseParen => {
                                                value
                                            }
                                            _ => {
                                                panic!("Invalid offset: {:?}", offset);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    _ => {
                        panic!("Invalid offset: {:?}", offset);
                    }
                }
            }
        };

        transpiled.push(' ');
        transpiled.push_str(&value);
        transpiled.push(' ');

        match offset {
            None => {
                transpiled.push_str(&4.to_string());
            }
            Some(offset) => {
                transpiled.push_str(&offset);
            }
        }

        println!("Transpiled: {}", transpiled);

        (transpiled, labels)
    }

    fn parse_label(&self, tokens: &[Token]) -> Option<String> {
        if let Token::Identifier(label) = &tokens[0] {
            if let Some(token) = tokens.get(1) {
                if let Token::Colon = token {
                    Some(label.clone())
                } else {
                    None
                }
            } else {
                None
            }
        } else {
            None
        }
    }

    fn tokenize_line(&self, line: &str) -> Vec<Token> {
        let mut tokens = Vec::new();
        let mut chars = line.chars();
        while let Some(c) = chars.next() {
            match c {
                'a'..='z' | 'A'..='Z' => {
                    let mut identifier = String::new();
                    identifier.push(c);
                    let mut colon = false;
                    while let Some(c) = chars.next() {
                        match c {
                            'a'..='z' | 'A'..='Z' | '0'..='9' | '_' => {
                                identifier.push(c);
                            }
                            ':' => {
                                colon = true;
                                break;
                            }
                            _ => {
                                break;
                            }
                        }
                    }
                    tokens.push(Token::Identifier(identifier));
                    if colon {
                        tokens.push(Token::Colon);
                    }
                }
                '0'..='9' | '-' => {
                    let mut number = String::new();
                    number.push(c);
                    let mut closed = false;
                    while let Some(c) = chars.next() {
                        match c {
                            '0'..='9' => {
                                number.push(c);
                            }
                            ')' => {
                                closed = true;
                                break;
                            }
                            _ => {
                                break;
                            }
                        }
                    }
                    tokens.push(Token::Number(number.parse().unwrap()));
                    if closed {
                        tokens.push(Token::CloseParen);
                    }
                }
                ':' => {
                    tokens.push(Token::Colon);
                }
                '(' => {
                    tokens.push(Token::OpenParen);
                }
                ')' => {
                    tokens.push(Token::CloseParen);
                }
                _ => {}
            }
        }
        tokens
    }
}

#[derive(Debug)]
enum Token {
    Identifier(String),
    Number(i32),
    Colon,
    OpenParen,
    CloseParen,
}