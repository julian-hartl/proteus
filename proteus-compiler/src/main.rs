use crate::lexer::lexer::Lexer;

mod lexer;
mod parser;

fn main() {
    // read input from console
    let mut input = String::new();
    std::io::stdin().read_line(&mut input).unwrap();
    let mut lexer = Lexer::new(input);
    while lexer.has_next() {
        let token = lexer.next_token();
        println!("{:?}", token);
    }
}
