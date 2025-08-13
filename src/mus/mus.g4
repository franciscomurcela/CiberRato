grammar mus;

@parser::header {
import java.util.Map;
import java.util.HashMap;
}

@parser::members {
static protected Map<String,Symbol> symbolTable = new HashMap<>();
}

main: block* EOF;

block: agent|behavior ;

agent:
	'agent' ':' 
	    (command? ';')*
	'end'
    ;

behavior:
	'behavior' ID ':' 
	    (input? ';')*
	    (command? ';')* 
	'end'
    ;

type returns[Type res]:
     'integer' {$res = new IntegerType();}
   | 'real'    {$res = new RealType();}
   | 'angle'   {$res = new AngleType();}
   | 'string'  {$res = new StringType();}
   | 'scene'   {$res = new SceneType();}
   ;

input:
	'input' type ID ('=' expr)? 
    ;

loadCommand: 
	'load' (String | ID)
    ;	

command:
    	print
    |	connect
    |	wait_command
    |	stop
    |	var	
    |	assignment
    |	sense
    |	rotate	
    |	turnAction 
    |	speed
    |	run
    |   loadCommand
    ;

connect:
	'connect' 'with' (name)? (',' host)? (',' port)?  
    ;

name returns[String varName]:
	'name' '=' expr
    ;

host returns[String varName]:
	'host' '=' expr
    ;

port returns[String varName]:
	'port' '=' expr 
    ;

assignment:
	ID ':=' expr
    ;

expr returns[Type eType, String varName]
    : e1=expr op=('*'|'/'|'%') e2=expr        #ExprOp
    | e1=expr op=('+'|'-') e2=expr            #ExprOp
    | e1=expr op=('=='|'!='|'<'|'>'|'<='|'>=') e2=expr #ExprOp
    | op=('+'|'-') expr                       #ExprUnitary
    | String                                  #ExprString
    | Real                                    #ExprReal
    | Integer                                 #ExprInteger
    | '(' expr ')'                            #ExprParent
    | sense                                   #ExprSense
    | read                                    #ExprRead
    | loadCommand                             #ExprLoad
    | typeCast                                #ExprTypeCast
    | ID                                      #ExprID
    ;

typeCast returns[String varName]:
	op=('integer'|'real'|'angle'|'string') '(' expr ')'
    ;

read returns[String varName]:
	'read' String 
    ;

var:
    'var' type ID ( '=' expr )?
    ;

print:
	'print' expr (',' expr)*
    ;

wait_command:
	'wait' expr
    ;

stop:
	'stop'
    ;

speed:
	'speed at' expr
    ;

twistAction: 
	'twist' direction=('left'|'right') expr
    ;

rotate:
	'rotate' (direction=('left'|'right'))? 'at' expr
    ;
run:
	'run' ID ('with' args (',' args)*)?
    ;

args returns[String varName]:
	ID '=' expr
    ;
	
turnAction: 
	'turn' ledName 'led' value=('on' | 'off')
    ;

ledName: 
	'visiting'
    | 	'returning'
    | 	'finish'
    ;

sense returns[String varName]:
	op=('compass' | 'beacon') id=Integer? 'sense' 
    ;
	

String: '"' .*? '"';
Real: [0-9]+[.][0-9]+;
Integer: [0-9]+;
ID: [a-zA-Z0-9]([a-zA-Z_0-9]*)?;
WS: [ \r\n\t] -> skip;
LCOMMENT: '//' ~[\n]* -> skip;
BCOMMENT: '/*' .*? '*/' -> skip;

alias:
    'alias' type ID '=' expr
    ;
