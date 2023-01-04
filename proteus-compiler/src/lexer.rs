pub mod lexer {
    use std::any::Any;

    pub struct Lexer {
        input: String,
        position: usize,
    }

    #[derive(Debug, Eq, PartialEq, Clone)]
    pub enum TokenKind {
        Number,
        WhiteSpace,
        Plus,
        Minus,
        Multiply,
        Divide,
        OpenParenthesis,
        CloseParenthesis,
        BadToken,
        EndOfFile,
    }

    #[derive(Debug)]
    pub struct SyntaxToken<T = Box<dyn Any>>   {
        pub(crate) token_kind: TokenKind,
        pub(crate) literal: String,
        start: usize,
        value: T,
    }
    
    impl SyntaxToken {
        pub fn mv (self)->Self {
            SyntaxToken {
                token_kind: self.token_kind,
                literal: self.literal,
                start: self.start,
                value: self.value,
            }
        }
    }

    impl Lexer {
        pub fn new(input: String) -> Lexer {
            Lexer {
                input,
                position: 0,
            }
        }

        fn current_char(&self) -> char {
            self.input.chars().nth(self.position).or_else(|| Some('\0')).unwrap()
        }

        fn next(&mut self) {
            self.position += 1;
        }

        fn is_current_digit(&self) -> bool {
            self.current_char().is_digit(10)
        }

        fn is_current_operator(&self) -> bool {
            self.current_char() == '+' || self.current_char() == '-' || self.current_char() == '*' || self.current_char() == '/'
        }

        fn is_current_parenthesis(&self) -> bool {
            self.current_char() == '(' || self.current_char() == ')'
        }

        fn token_from_operator(&self, operator: char) -> TokenKind {
            match operator {
                '+' => TokenKind::Plus,
                '-' => TokenKind::Minus,
                '*' => TokenKind::Multiply,
                '/' => TokenKind::Divide,
                _ => TokenKind::BadToken,
            }
        }

        pub fn has_next(&self) -> bool {
            self.position <= self.input.len()
        }

        pub fn next_token(&mut self) -> SyntaxToken {
            if self.position >= self.input.len() {
                let start = self.position;
                self.next();
                return SyntaxToken {
                    token_kind: TokenKind::EndOfFile,
                    literal: String::from(""),
                    start,
                    value: Box::new(0),
                };
            }
            // check if current char is a digit
            if self.is_current_digit() {
                let start = self.position;

                while self.is_current_digit() {
                    self.next();
                }

                let length = self.position - start;
                let literal = self.input.chars().skip(start).take(length).collect::<String>();
                let value = literal.parse::<i32>().unwrap();
                return SyntaxToken {
                    token_kind: TokenKind::Number,
                    literal,
                    start,
                    value: Box::new(value),
                };
            }
            if self.current_char().is_whitespace() {
                let start = self.position;

                while self.current_char().is_whitespace() {
                    self.next();
                }

                let length = self.position - start;
                let literal = self.input.chars().skip(start).take(length).collect::<String>();
                return SyntaxToken {
                    token_kind: TokenKind::WhiteSpace,
                    value: Box::new(literal.clone()),
                    literal,
                    start,
                };
            }
            if self.is_current_operator() {
                let start = self.position;
                let c = self.current_char();
                let literal = c.to_string();
                self.next();
                return SyntaxToken {
                    token_kind: self.token_from_operator(c),
                    value: Box::new(literal.clone()),
                    literal,
                    start,
                };
            } else if self.current_char().eq(&'(') {
                let start = self.position;
                self.next();
                return SyntaxToken {
                    token_kind: TokenKind::OpenParenthesis,
                    literal: String::from("("),
                    start,
                    value: Box::new(&"("),
                };
            } else if self.current_char().eq(&')') {
                let start = self.position;
                self.next();
                return SyntaxToken {
                    token_kind: TokenKind::CloseParenthesis,
                    literal: String::from(")"),
                    start,
                    value: Box::new(&")"),
                };
            }
            let start = self.position;
            let c = self.current_char();
            self.next();
            SyntaxToken {
                token_kind: TokenKind::BadToken,
                literal: String::from(c),
                start,
                value: Box::new(c),
            }
        }
    }
}

