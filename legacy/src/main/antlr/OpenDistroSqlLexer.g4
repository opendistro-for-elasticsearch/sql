/*
MySQL (Positive Technologies) grammar
The MIT License (MIT).
Copyright (c) 2015-2017, Ivan Kochurkin (kvanttt@gmail.com), Positive Technologies.
Copyright (c) 2017, Ivan Khudyashev (IHudyashov@ptsecurity.com)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

lexer grammar OpenDistroSqlLexer;

channels { SQLCOMMENT, ERRORCHANNEL }


// SKIP

SPACE:                              [ \t\r\n]+    -> channel(HIDDEN);
SPEC_SQL_COMMENT:                   '/*!' .+? '*/' -> channel(SQLCOMMENT);
COMMENT_INPUT:                      '/*' .*? '*/' -> channel(HIDDEN);
LINE_COMMENT:                       (
                                      ('-- ' | '#') ~[\r\n]* ('\r'? '\n' | EOF)
                                      | '--' ('\r'? '\n' | EOF)
                                    ) -> channel(HIDDEN);


// Keywords
// Common Keywords

ALL:                                'ALL';
AND:                                'AND';
AS:                                 'AS';
ASC:                                'ASC';
BETWEEN:                            'BETWEEN';
BY:                                 'BY';
CASE:                               'CASE';
CAST:                               'CAST';
CROSS:                              'CROSS';
DATETIME:                           'DATETIME';
DELETE:                             'DELETE';
DESC:                               'DESC';
DESCRIBE:                           'DESCRIBE';
DISTINCT:                           'DISTINCT';
DOUBLE:                             'DOUBLE';
ELSE:                               'ELSE';
EXISTS:                             'EXISTS';
FALSE:                              'FALSE';
FLOAT:                              'FLOAT';
FROM:                               'FROM';
GROUP:                              'GROUP';
HAVING:                             'HAVING';
IN:                                 'IN';
INNER:                              'INNER';
INT:                                'INT';
IS:                                 'IS';
JOIN:                               'JOIN';
LEFT:                               'LEFT';
LIKE:                               'LIKE';
LIMIT:                              'LIMIT';
LONG:                               'LONG';
MATCH:                              'MATCH';
NATURAL:                            'NATURAL';
NOT:                                'NOT';
NULL_LITERAL:                       'NULL';
ON:                                 'ON';
OR:                                 'OR';
ORDER:                              'ORDER';
OUTER:                              'OUTER';
REGEXP:                             'REGEXP';
RIGHT:                              'RIGHT';
SELECT:                             'SELECT';
SHOW:                               'SHOW';
STRING:                             'STRING';
THEN:                               'THEN';
TRUE:                               'TRUE';
UNION:                              'UNION';
USING:                              'USING';
WHEN:                               'WHEN';
WHERE:                              'WHERE';


// OD SQL special keyword
MISSING:                            'MISSING';
EXCEPT:                             'MINUS';


// Group function Keywords

AVG:                                'AVG';
COUNT:                              'COUNT';
MAX:                                'MAX';
MIN:                                'MIN';
SUM:                                'SUM';


// Common function Keywords

SUBSTRING:                          'SUBSTRING';
TRIM:                               'TRIM';
YEAR:                               'YEAR';


// Keywords, but can be ID
// Common Keywords, but can be ID

END:                                'END';
FULL:                               'FULL';
OFFSET:                             'OFFSET';


// PRIVILEGES

TABLES:                             'TABLES';


// Common function names

ABS:                                'ABS';
ACOS:                               'ACOS';
ADD:                                'ADD';
ASCII:                              'ASCII';
ASIN:                               'ASIN';
ATAN:                               'ATAN';
ATAN2:                              'ATAN2';
CBRT:                               'CBRT';
CEIL:                               'CEIL';
CONCAT:                             'CONCAT';
CONCAT_WS:                          'CONCAT_WS';
COS:                                'COS';
COSH:                               'COSH';
COT:                                'COT';
CURDATE:                            'CURDATE';
DATE:                               'DATE';
DATE_FORMAT:                        'DATE_FORMAT';
DAYOFMONTH:                         'DAYOFMONTH';
DEGREES:                            'DEGREES';
E:                                  'E';
EXP:                                'EXP';
EXPM1:                              'EXPM1';
FLOOR:                              'FLOOR';
IF:                                 'IF';
IFNULL:                             'IFNULL';
ISNULL:                             'ISNULL';
LENGTH:                             'LENGTH';
LN:                                 'LN';
LOCATE:                             'LOCATE';
LOG:                                'LOG';
LOG10:                              'LOG10';
LOG2:                               'LOG2';
LOWER:                              'LOWER';
LTRIM:                              'LTRIM';
MAKETIME:                           'MAKETIME';
MODULUS:                            'MODULUS';
MONTH:                              'MONTH';
MONTHNAME:                          'MONTHNAME';
MULTIPLY:                           'MULTIPLY';
NOW:                                'NOW';
PI:                                 'PI';
POW:                                'POW';
POWER:                              'POWER';
RADIANS:                            'RADIANS';
RAND:                               'RAND';
REPLACE:                            'REPLACE';
RINT:                               'RINT';
ROUND:                              'ROUND';
RTRIM:                              'RTRIM';
SIGN:                               'SIGN';
SIGNUM:                             'SIGNUM';
SIN:                                'SIN';
SINH:                               'SINH';
SQRT:                               'SQRT';
SUBTRACT:                           'SUBTRACT';
TAN:                                'TAN';
TIMESTAMP:                          'TIMESTAMP';
UPPER:                              'UPPER';

D:                                  'D';
T:                                  'T';
TS:                                 'TS';
LEFT_BRACE:                         '{';
RIGHT_BRACE:                        '}';


// OD SQL special functions
DATE_HISTOGRAM:                     'DATE_HISTOGRAM';
DAY_OF_MONTH:                       'DAY_OF_MONTH';
DAY_OF_YEAR:                        'DAY_OF_YEAR';
DAY_OF_WEEK:                        'DAY_OF_WEEK';
EXCLUDE:                            'EXCLUDE';
EXTENDED_STATS:                     'EXTENDED_STATS';
FIELD:                              'FIELD';
FILTER:                             'FILTER';
GEO_BOUNDING_BOX:                   'GEO_BOUNDING_BOX';
GEO_CELL:                           'GEO_CELL';
GEO_DISTANCE:                       'GEO_DISTANCE';
GEO_DISTANCE_RANGE:                 'GEO_DISTANCE_RANGE';
GEO_INTERSECTS:                     'GEO_INTERSECTS';
GEO_POLYGON:                        'GEO_POLYGON';
HISTOGRAM:                          'HISTOGRAM';
HOUR_OF_DAY:                        'HOUR_OF_DAY';
INCLUDE:                            'INCLUDE';
IN_TERMS:                           'IN_TERMS';
MATCHPHRASE:                        'MATCHPHRASE';
MATCH_PHRASE:                       'MATCH_PHRASE';
MATCHQUERY:                         'MATCHQUERY';
MATCH_QUERY:                        'MATCH_QUERY';
MINUTE_OF_DAY:                      'MINUTE_OF_DAY';
MINUTE_OF_HOUR:                     'MINUTE_OF_HOUR';
MONTH_OF_YEAR:                      'MONTH_OF_YEAR';
MULTIMATCH:                         'MULTIMATCH';
MULTI_MATCH:                        'MULTI_MATCH';
NESTED:                             'NESTED';
PERCENTILES:                        'PERCENTILES';
REGEXP_QUERY:                       'REGEXP_QUERY';
REVERSE_NESTED:                     'REVERSE_NESTED';
QUERY:                              'QUERY';
RANGE:                              'RANGE';
SCORE:                              'SCORE';
SECOND_OF_MINUTE:                   'SECOND_OF_MINUTE';
STATS:                              'STATS';
TERM:                               'TERM';
TERMS:                              'TERMS';
TOPHITS:                            'TOPHITS';
WEEK_OF_YEAR:                       'WEEK_OF_YEAR';
WILDCARDQUERY:                      'WILDCARDQUERY';
WILDCARD_QUERY:                     'WILDCARD_QUERY';


// Operators

// Operators. Arithmetics

STAR:                               '*';
DIVIDE:                             '/';
MODULE:                             '%';
PLUS:                               '+';
MINUS:                              '-';
DIV:                                'DIV';
MOD:                                'MOD';


// Operators. Comparation

EQUAL_SYMBOL:                       '=';
GREATER_SYMBOL:                     '>';
LESS_SYMBOL:                        '<';
EXCLAMATION_SYMBOL:                 '!';


// Operators. Bit

BIT_NOT_OP:                         '~';
BIT_OR_OP:                          '|';
BIT_AND_OP:                         '&';
BIT_XOR_OP:                         '^';


// Constructors symbols

DOT:                                '.';
LR_BRACKET:                         '(';
RR_BRACKET:                         ')';
COMMA:                              ',';
SEMI:                               ';';
AT_SIGN:                            '@';
ZERO_DECIMAL:                       '0';
ONE_DECIMAL:                        '1';
TWO_DECIMAL:                        '2';
SINGLE_QUOTE_SYMB:                  '\'';
DOUBLE_QUOTE_SYMB:                  '"';
REVERSE_QUOTE_SYMB:                 '`';
COLON_SYMB:                         ':';


// Literal Primitives

START_NATIONAL_STRING_LITERAL:      'N' SQUOTA_STRING;
STRING_LITERAL:                     SQUOTA_STRING;
DECIMAL_LITERAL:                    DEC_DIGIT+;
HEXADECIMAL_LITERAL:                'X' '\'' (HEX_DIGIT HEX_DIGIT)+ '\''
                                    | '0X' HEX_DIGIT+;

REAL_LITERAL:                       (DEC_DIGIT+)? '.' DEC_DIGIT+
                                    | DEC_DIGIT+ '.' EXPONENT_NUM_PART
                                    | (DEC_DIGIT+)? '.' (DEC_DIGIT+ EXPONENT_NUM_PART)
                                    | DEC_DIGIT+ EXPONENT_NUM_PART;
NULL_SPEC_LITERAL:                  '\\' 'N';
BIT_STRING:                         BIT_STRING_L;



// Hack for dotID
// Prevent recognize string:        .123somelatin AS ((.123), FLOAT_LITERAL), ((somelatin), ID)
//  it must recoginze:              .123somelatin AS ((.), DOT), (123somelatin, ID)

DOT_ID:                             '.' ID_LITERAL;



// Identifiers

ID:                                 ID_LITERAL;
// DOUBLE_QUOTE_ID:                 '"' ~'"'+ '"';
REVERSE_QUOTE_ID:                   '`' ~'`'+ '`';
DOUBLE_QUOTE_ID:                    DQUOTA_STRING;
BACKTICK_QUOTE_ID:                  BQUOTA_STRING;
STRING_USER_NAME:                   (
                                        SQUOTA_STRING | DQUOTA_STRING
                                        | BQUOTA_STRING | ID_LITERAL
                                    ) '@'
                                    (
                                        SQUOTA_STRING | DQUOTA_STRING
                                        | BQUOTA_STRING | ID_LITERAL
                                    );


// Fragments for Literal primitives

fragment EXPONENT_NUM_PART:         'E' [-+]? DEC_DIGIT+;
fragment ID_LITERAL:                [A-Z_$0-9@]*?[A-Z_$]+?[A-Z_$\-0-9]*;
fragment DQUOTA_STRING:             '"' ( '\\'. | '""' | ~('"'| '\\') )* '"';
fragment SQUOTA_STRING:             '\'' ('\\'. | '\'\'' | ~('\'' | '\\'))* '\'';
fragment BQUOTA_STRING:             '`' ( '\\'. | '``' | ~('`'|'\\'))* '`';
fragment HEX_DIGIT:                 [0-9A-F];
fragment DEC_DIGIT:                 [0-9];
fragment BIT_STRING_L:              'B' '\'' [01]+ '\'';



// Last tokens must generate Errors

ERROR_RECOGNITION:                  .    -> channel(ERRORCHANNEL);
