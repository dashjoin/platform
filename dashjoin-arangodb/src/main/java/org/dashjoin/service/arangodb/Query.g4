grammar Query;

query
   : 'FOR' VAR 'IN' VAR sort? limit? filter* 'RETURN' obj
   ;

sort
   : 'SORT' VAR '.' VAR ('DESC')?
   ;

limit
   : 'LIMIT' INT ',' INT
   | 'LIMIT' INT
   ;

obj
   : '{' pair (',' pair)* '}'
   | '{' '}'
   ;

pair
   : STRING ':' VAR '.' VAR
   ;

filter
   : 'FILTER' VAR '.' VAR '==' value
   ;

value 
   : INT
   | STRING
   ;

STRING
   : '"' (~["])* '"'
   ;

// variable
VAR
   : [a-zA-Z_][a-zA-Z0-9_]*
   ;

INT
   : [0-9]+
   ;

// skip whitespaces
WS
   : [ \n\t\r]+ -> skip
   ;
