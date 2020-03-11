/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

lexer grammar PPLLexer;

channels { WHITESPACE, ERRORCHANNEL }


// COMMAND KEYWORDS
SEARCH:                             'SEARCH';
TOP:                                'TOP';
FROM:                               'FROM';
WHERE:                              'WHERE';
LIMIT:                              'LIMIT';
BY:                                 'BY';
COUNTFIELD:                         'COUNTFIELD';
SOURCE:                             'SOURCE';
INDEX:                              'INDEX';
FIELDS:                             'FIELDS';
OTHERSTR:                           'OTHERSTR';
PERCENTFIELD:                       'PERCENTFIELD';
SHOWCOUNT:                          'SHOWCOUNT';
SHOWPERC:                           'SHOWPERC';
USEOTHER:                           'USEROTHER';



// COMPARISON FUNCTION KEYWORDS
CASE:                               'CASE';
IN:                                 'IN';


// MATH FUNCTION KEYWORDS
ABS:                                'ABS';
POW:                                'POW';


// AGGREGATE FUNCTION KEYWORDS
AVG:                                'AVG';
COUNT:                              'COUNT';


// LOGICAL KEYWORDS
NOT:                                'NOT';
OR:                                 'OR';
AND:                                'AND';
TRUE:                               'TRUE';
FALSE:                              'FALSE';


// DATASET TYPE
DATAMODEL:                          'DATAMODEL';
LOOKUP:                             'LOOKUP';
SAVEDSEARCH:                        'SAVEDSEARCH';


// SPECIAL CHARS AND OPERATORS
PIPE:                               '|';
COMMA:                              ',';
DOT:                                '.';
EQUAL_SYMBOL:                       '=';
GREATER_SYMBOL:                     '>';
LESS_SYMBOL:                        '<';
NOT_GREATER:                        '<' '=';
NOT_LESS:                           '>' '=';
NOT_EQUAL:                          '!' '=';
PLUS:                               '+';
MINUS:                              '-';
STAR:                               '*';
DIVIDE:                             '/';
MODULE:                             '%';
EXCLAMATION_SYMBOL:                 '!';
COLON:                              ':';
LT_PRTHS:                           '(';
RT_PRTHS:                           ')';


STRING_LITERAL:                     DQUOTA_STRING | SQUOTA_STRING | BQUOTA_STRING;
ID:                                 ID_LITERAL;
DOT_ID:                             '.' ID;
DECIMAL_LITERAL:                    DEC_DIGIT+;

fragment ID_LITERAL:                [A-Z_$0-9@]*?[A-Z_$\-]+?[A-Z_$\-0-9]*;
fragment DQUOTA_STRING:             '"' ( '\\'. | '""' | ~('"'| '\\') )* '"';
fragment SQUOTA_STRING:             '\'' ('\\'. | '\'\'' | ~('\'' | '\\'))* '\'';
fragment BQUOTA_STRING:             '`' ( '\\'. | '``' | ~('`'|'\\'))* '`';
fragment DEC_DIGIT:                 [0-9];



ERROR_RECOGNITION:                  .    -> channel(ERRORCHANNEL);