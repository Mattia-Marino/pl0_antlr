grammar PL0;

program
    : block PERIOD
    ;

block
    : (constDeclaration)? (varDeclaration)? (procedureDeclaration)* statement
    ;

// --- Declarations ---

constDeclaration
    : K_CONST IDENTIFIER '=' NUMBER (COMMA IDENTIFIER '=' NUMBER)* SEMI
    ;

varDeclaration
    : K_VAR IDENTIFIER (COMMA IDENTIFIER)* SEMI
    ;

procedureDeclaration
    : K_PROCEDURE IDENTIFIER SEMI block SEMI
    ;

// --- Statements ---

statement
    : assignmentStatement
    | callStatement
    | beginStatement
    | ifStatement
    | whileStatement
    | readStatement
    | writeStatement
    | skipStatement
    ;

skipStatement
    : // empty rule for an implied empty statement (e.g., just a semicolon)
    ;

assignmentStatement
    : IDENTIFIER ASSIGN expression
    ;

callStatement
    : K_CALL IDENTIFIER
    ;

// Allows statements to be separated by semicolons
beginStatement
    : K_BEGIN statement (SEMI statement)* K_END
    ;

ifStatement
    : K_IF condition K_THEN statement (K_ELSE statement)?
    ;

whileStatement
    : K_WHILE condition K_DO statement
    ;

readStatement
    : K_READ LPAREN IDENTIFIER (COMMA IDENTIFIER)* RPAREN
    | K_READ IDENTIFIER (COMMA IDENTIFIER)*
    | QUESTION IDENTIFIER (COMMA IDENTIFIER)*
    ;

writeStatement
    : K_WRITE ( LPAREN expression (COMMA expression)* RPAREN
              | expression (COMMA expression)*
              )
    | BANG expression (COMMA expression)*
    ;

// --- Expressions and Conditions ---

condition
    : K_ODD expression
    | expression relation expression
    ;

relation
    : '=' | '#' | '<' | '<=' | '>' | '>='
    ;

expression
    : ('+' | '-')? term (('+' | '-') term)*
    ;

term
    : factor (('*' | '/') factor)*
    ;

factor
    : IDENTIFIER
    | NUMBER
    | LPAREN expression RPAREN
    ;

// --- Lexer Rules (Tokens) ---

// Keywords (case-insensitive)
K_CONST     : [cC][oO][nN][sS][tT] ;
K_VAR       : [vV][aA][rR] ;
K_PROCEDURE : [pP][rR][oO][cC][eE][dD][uU][rR][eE] ;
K_CALL      : [cC][aA][lL][lL] ;
K_BEGIN     : [bB][eE][gG][iI][nN] ;
K_END       : [eE][nN][dD] ;
K_IF        : [iI][fF] ;
K_THEN      : [tT][hH][eE][nN] ;
K_ELSE      : [eE][lL][sS][eE] ;
K_WHILE     : [wW][hH][iI][lL][eE] ;
K_DO        : [dD][oO] ;
K_ODD       : [oO][dD][dD] ;
K_READ      : [rR][eE][aA][dD] ;
K_WRITE     : [wW][rR][iI][tT][eE] ;

// Operators and Punctuation
ASSIGN      : ':=' ;
SEMI        : ';' ;
COMMA       : ',' ;
LPAREN      : '(' ;
RPAREN      : ')' ;
PERIOD      : '.' ;
// Short write-shorthand
BANG        : '!' ;
// Short read-shorthand
QUESTION    : '?' ;

// Identifiers and Numbers
IDENTIFIER  : LETTER (LETTER | DIGIT)* ;
NUMBER      : DIGIT+ ;

// Basic Types
fragment LETTER : [a-zA-Z] ;
fragment DIGIT  : [0-9] ;

// Whitespace (Ignored by the parser)
WS          : [ \t\r\n]+ -> skip ;
