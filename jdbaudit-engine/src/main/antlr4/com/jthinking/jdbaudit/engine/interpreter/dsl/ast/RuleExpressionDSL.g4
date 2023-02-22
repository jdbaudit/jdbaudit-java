grammar RuleExpressionDSL;

/** PARSER */
line : expr EOF ;
expr
    : '(' expr ')'          # parenExpr
    | '!' expr              # notEpr
    | expr '+' expr          # andEpr
    | expr '|' expr          # orEpr
    | ID                    # identifier
;

/** LEXER */
WS : [ \t\n\r]+ -> skip ;
ID : ALPHABET+ ;
fragment DIGIT : '0'..'9';
fragment ALPHABET: [a-zA-Z];
