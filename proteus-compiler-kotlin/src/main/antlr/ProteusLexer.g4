lexer grammar ProteusLexer;

// Keywords

BREAK:              'break';
CONTINUE:           'continue';
ELSE:               'else';
FOR:                'for';
IF:                 'if';
IMPORT:             'import';
RETURN:             'return';
WHILE:              'while';
IS:                 'is';
FN:                 'fn';
IN:                 'in';
UNTIL:              'until';


// Variable Declaration
VAR:                'var';
VAL:                'val';
CONST:              'const';

// Literals
INT_LITERAL:        Digits;

BOOL_LITERAL:       'true'
            |       'false'
            ;

STRING_LITERAL:     '"' (~["\\\r\n] | EscapeSequence)* '"';


// Separators

LPAREN:             '(';
RPAREN:             ')';
LBRACE:             '{';
RBRACE:             '}';
SEMI:               ';';
COMMA:              ',';

// Operators

ASSIGN:             '=';
GT:                 '>';
LT:                 '<';
NOT:               'not';
TILDE:              '~';
COLON:              ':';
EQUAL:              '==';
LE:                 '<=';
GE:                 '>=';
NOTEQUAL:           '!=';
AND:                'and';
OR:                 'or';
XOR:                 'xor';
ADD:                '+';
SUB:                '-';
MUL:                '*';
EXPO:                '**';
DIV:                '/';
BITAND:             '&';
BITOR:              '|';
CARET:              '^';
MOD:                '%';

ADD_ASSIGN:         '+=';
SUB_ASSIGN:         '-=';

ARROW:              '->';


// Whitespace and comments

WS:                 [ \t\r\n\u000C]+ -> channel(HIDDEN);
COMMENT:            '/*' .*? '*/'    -> channel(HIDDEN);
LINE_COMMENT:       '//' ~[\r\n]*    -> channel(HIDDEN);

// Identifiers

IDENTIFIER:         Letter LetterOrDigit*;

// Fragment rules


fragment EscapeSequence
    : '\\' [btnfr"'\\]
    | '\\' ([0-3]? [0-7])? [0-7]
    ;

fragment Digits
    : [0-9] ([0-9_]* [0-9])?
    ;

fragment LetterOrDigit
    : Letter
    | [0-9]
    ;

fragment Letter
    : [a-zA-Z$_] // these are the "java letters" below 0x7F
    | ~[\u0000-\u007F\uD800-\uDBFF] // covers all characters above 0x7F which are not a surrogate
    | [\uD800-\uDBFF] [\uDC00-\uDFFF] // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
    ;
