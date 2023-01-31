parser grammar ProteusParser;
// http://lab.antlr.org/
options { tokenVocab=ProteusLexer; }

compilationUnit
    : importDeclaration* memberDeclaration* EOF
    ;


importDeclaration
    : IMPORT STRING_LITERAL ';'
    ;

memberDeclaration
    : functionDeclaration
    | variableDeclaration;

identifier
    : IDENTIFIER
    ;

/* We use rule this even for void methods which cannot have [] after parameters.
   This simplifies grammar and we can consider void to be a type, which
   renders the [] matching as a context-sensitive issue or a semantic check
   for invalid return type after parsing.
 */
functionDeclaration
    : FN identifier '(' functionParameterList? ')' returnTypeClause? statement
    ;

functionParameterList
    : functionParameter (',' functionParameter)*
    ;

functionParameter
    : identifier typeClause?
    ;

typeClause
    : ':' identifier
    ;

returnTypeClause
    : '->' identifier
    ;


variableDeclaration
    : variableModifier identifier typeClause? '=' expression ';'
    ;

variableModifier
    : 'var'
    | 'val'
    | 'const'
    ;

literal
    : INT_LITERAL
    | STRING_LITERAL
    | BOOL_LITERAL
    ;

// STATEMENTS / BLOCKS


statement
    : '{' statement* '}'
    | IF expression statement (ELSE statement)?
    | FOR '(' forControl ')' statement
    | WHILE expression statement
    | RETURN expression? ';'
    | BREAK ';'
    | CONTINUE  ';'
    | variableDeclaration
    | SEMI
    ;
forControl
    : identifier IN INT_LITERAL UNTIL INT_LITERAL
    ;


// EXPRESSIONS

expressionList
    : expression (',' expression)*
    ;

methodCall
    : identifier '(' expressionList? ')'
    ;

expression
    : primary
    | methodCall
    | prefix=('+'|'-') expression
    | prefix=('~'|'not') expression
    | expression bop='**' expression
    | expression bop=('*'|'/'|'%') expression
    | expression bop=('+'|'-') expression
    | expression ('<' '<' | '>' '>' '>' | '>' '>') expression
    | expression bop=('<=' | '>=' | '>' | '<') expression
    | expression bop=IS identifier
    | expression bop=('==' | '!=') expression
    | expression bop='&' expression
    | expression bop='^' expression
    | expression bop='|' expression
    | expression bop='and' expression
    | expression bop='xor' expression
    | expression bop='or' expression
    | <assoc=right> expression
      bop=('=' | '+=' | '-=' )
      expression

    ;

primary
    : '(' expression ')'
    | literal
    | identifier
    ;

