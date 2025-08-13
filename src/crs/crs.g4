grammar crs;

scene: statement* EOF;

statement:
      realDecl semiOpt
    | beaconDecl semiOpt
    | targetDecl semiOpt
    | wallDecl semiOpt
    | spotDecl semiOpt
    | assignment semiOpt
    ;

realDecl: 'real' ':' assignmentList;
beaconDecl: 'beacon' ':' 'position' '=' '(' decimal ',' decimal ')' ',' 'height' '=' decimal;
targetDecl: 'target' ':' 'position' '=' '(' decimal ',' decimal ')' ',' 'radius' '=' decimal;
wallDecl: 'wall' ':' 'height' '=' decimal ',' 'corners' '=' coordList;
spotDecl: 'spot' ':' 'position' '=' '(' decimal ',' decimal ')' ',' 'heading' '=' decimal degreesOpt;

degreesOpt: 'degrees'? ;

assignment: ID '=' decimal;
assignmentList: assignment (',' assignment)*;

coordList: coordinate ('-' coordinate)*;
coordinate: '(' decimal ',' decimal ')' 
          | INCREMENT '(' decimal ',' decimal ')';

decimal: NUMBER | ID;

semiOpt: ';'? ;

ID: [A-Za-z][A-Za-z0-9_]*;
NUMBER: '-'? [0-9]+ ('.' [0-9]+)?;
WS: [ \t\r\n]+ -> skip;
COMMENT: '#' ~[\r\n]* -> skip;
INCREMENT: '++';