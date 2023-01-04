pub mod parser {
    use crate::lexer::lexer::{Lexer, SyntaxToken, TokenKind};

    pub enum SyntaxKind {
        NumberExpression,
        BinaryExpression,
    }

    pub struct SyntaxNode<'a> {
        kind: SyntaxKind,
        token: &'a SyntaxToken,
        children: Option<Vec<&'a SyntaxNode<'a>>>,
    }

    impl <'a> SyntaxNode<'a> {
        pub fn number(token: &'a SyntaxToken) -> SyntaxNode<'a> {
            SyntaxNode {
                kind: SyntaxKind::NumberExpression,
                token,
                children: None,
            }
        }

        pub fn binary(left: &'a SyntaxNode<'a>, operator: &'a SyntaxToken, right: &'a SyntaxNode<'a>) -> SyntaxNode<'a> {
            SyntaxNode {
                kind: SyntaxKind::BinaryExpression,
                token: operator,
                children: Some(vec![left, right]),
            }
        }
    }

    pub struct Parser {
        tokens: Vec<SyntaxToken>,
        position: usize,
    }

    impl Parser {
        pub fn new(input: String) -> Self {
            let tokens = Parser::parse_input(input);
            Self {
                tokens,
                position: 0,
            }
        }
        fn next_token(&mut self) -> &SyntaxToken{
            self.position += 1;
            &self.tokens[self.position - 1]

        }

        pub fn parse_expression(&mut self) -> SyntaxNode {
            let mut left = self.parse_primary_expression();

            while self.current().token_kind == TokenKind::Plus || self.current().token_kind == TokenKind::Minus {
                let operator = self.next_token();
                let right = self.parse_primary_expression();
                left = SyntaxNode::binary(&left, operator, &right);
            }
            left
        }

        fn parse_primary_expression(&mut self) -> SyntaxNode {
            let token = self.next_token();
            SyntaxNode::number(token)
        }


        fn parse_input(input: String) -> Vec<SyntaxToken> {
            let mut lexer = Lexer::new(input);
            let mut tokens: Vec<SyntaxToken> = Vec::new();
            while {
                let token = lexer.next_token();
                let is_end_of_file = token.token_kind != TokenKind::EndOfFile;
                println!("{:?}", token);
                if token.token_kind != TokenKind::BadToken && token.token_kind != TokenKind::WhiteSpace {
                    tokens.push(token);
                }
                is_end_of_file
            } {}
            tokens
        }

        fn peek(&self, offset: usize) -> &SyntaxToken{
            let index = self.position + offset;
            if index >= self.tokens.len() {
                &self.tokens[self.tokens.len() - 1]
            } else {
                &self.tokens[index]
            }
        }

        fn current(&self) -> &SyntaxToken {
            self.peek(0)
        }
    }
}