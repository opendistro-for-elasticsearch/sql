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

lexer grammar OpenDistroPPLLexer;

channels { WHITESPACE, ERRORCHANNEL }


// COMMAND KEYWORDS
SEARCH:                             'SEARCH';
FROM:                               'FROM';
WHERE:                              'WHERE';
FIELDS:                             'FIELDS';
RENAME:                             'RENAME';
STATS:                              'STATS';
DEDUP:                              'DEDUP';
SORT:                               'SORT';
EVAL:                               'EVAL';
HEAD:                               'HEAD';
TOP:                                'TOP';
RARE:                               'RARE';

// COMMAND ASSIST KEYWORDS
AS:                                 'AS';
BY:                                 'BY';
SOURCE:                             'SOURCE';
INDEX:                              'INDEX';
D:                                  'D';
DESC:                               'DESC';

// CLAUSE KEYWORDS
SORTBY:                             'SORTBY';

// FIELD KEYWORDS
AUTO:                               'AUTO';
STR:                                'STR';
IP:                                 'IP';
NUM:                                'NUM';

// ARGUMENT KEYWORDS
KEEPEMPTY:                          'KEEPEMPTY';
KEEPLAST:                           'KEEPLAST';
CONSECUTIVE:                        'CONSECUTIVE';
DEDUP_SPLITVALUES:                  'DEDUP_SPLITVALUES';
PARTITIONS:                         'PARTITIONS';
ALLNUM:                             'ALLNUM';
DELIM:                              'DELIM';
WHILE:                              'WHILE';

// COMPARISON FUNCTION KEYWORDS
CASE:                               'CASE';
IN:                                 'IN';

// LOGICAL KEYWORDS
NOT:                                'NOT';
OR:                                 'OR';
AND:                                'AND';
XOR:                                'XOR';
TRUE:                               'TRUE';
FALSE:                              'FALSE';
LIKE:                               'LIKE';
REGEXP:                             'REGEXP';

// DATETIME, INTERVAL AND UNIT KEYWORDS
DATETIME:                           'DATETIME';
INTERVAL:                           'INTERVAL';
MICROSECOND:                        'MICROSECOND';
SECOND:                             'SECOND';
MINUTE:                             'MINUTE';
HOUR:                               'HOUR';
DAY:                                'DAY';
WEEK:                               'WEEK';
MONTH:                              'MONTH';
QUARTER:                            'QUARTER';
YEAR:                               'YEAR';
SECOND_MICROSECOND:                 'SECOND_MICROSECOND';
MINUTE_MICROSECOND:                 'MINUTE_MICROSECOND';
MINUTE_SECOND:                      'MINUTE_SECOND';
HOUR_MICROSECOND:                   'HOUR_MICROSECOND';
HOUR_SECOND:                        'HOUR_SECOND';
HOUR_MINUTE:                        'HOUR_MINUTE';
DAY_MICROSECOND:                    'DAY_MICROSECOND';
DAY_SECOND:                         'DAY_SECOND';
DAY_MINUTE:                         'DAY_MINUTE';
DAY_HOUR:                           'DAY_HOUR';
YEAR_MONTH:                         'YEAR_MONTH';

// DATASET TYPES
DATAMODEL:                          'DATAMODEL';
LOOKUP:                             'LOOKUP';
SAVEDSEARCH:                        'SAVEDSEARCH';

// SPECIAL CHARACTERS AND OPERATORS
PIPE:                               '|';
COMMA:                              ',';
DOT:                                '.';
EQUAL:                              '=';
GREATER:                            '>';
LESS:                               '<';
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
LT_SQR_PRTHS:                       '[';
RT_SQR_PRTHS:                       ']';
SINGLE_QUOTE:                       '\'';
DOUBLE_QUOTE:                       '"';
BACKTICK:                           '`';

// AGGREGATIONS
AVG:                                'AVG';
COUNT:                              'COUNT';
DISTINCT_COUNT:                     'DISTINCT_COUNT';
ESTDC:                              'ESTDC';
ESTDC_ERROR:                        'ESTDC_ERROR';
MAX:                                'MAX';
MEAN:                               'MEAN';
MEDIAN:                             'MEDIAN';
MIN:                                'MIN';
MODE:                               'MODE';
RANGE:                              'RANGE';
STDEV:                              'STDEV';
STDEVP:                             'STDEVP';
SUM:                                'SUM';
SUMSQ:                              'SUMSQ';
VAR:                                'VAR';
VARP:                               'VARP';
PERCENTILE:                         'PERCENTILE';
FIRST:                              'FIRST';
LAST:                               'LAST';
LIST:                               'LIST';
VALUES:                             'VALUES';
EARLIEST:                           'EARLIEST';
EARLIEST_TIME:                      'EARLIEST_TIME';
LATEST:                             'LATEST';
LATEST_TIME:                        'LATEST_TIME';
PER_DAY:                            'PER_DAY';
PER_HOUR:                           'PER_HOUR';
PER_MINUTE:                         'PER_MINUTE';
PER_SECOND:                         'PER_SECOND';
RATE:                               'RATE';
SPARKLINE:                          'SPARKLINE';
C:                                  'C';
DC:                                 'DC';

// BASIC FUNCTIONS
ABS:                                'ABS';
CEIL:                               'CEIL';
CEILING:                            'CEILING';
CONV:                               'CONV';
CRC32:                              'CRC32';
E:                                  'E';
EXP:                                'EXP';
FLOOR:                              'FLOOR';
LN:                                 'LN';
LOG:                                'LOG';
LOG10:                              'LOG10';
LOG2:                               'LOG2';
MOD:                                'MOD';
PI:                                 'PI';
POW:                                'POW';
POWER:                              'POWER';
RAND:                               'RAND';
ROUND:                              'ROUND';
SIGN:                               'SIGN';
SQRT:                               'SQRT';
TRUNCATE:                           'TRUNCATE';

// TRIGONOMETRIC FUNCTIONS
ACOS:                               'ACOS';
ASIN:                               'ASIN';
ATAN:                               'ATAN';
ATAN2:                              'ATAN2';
COS:                                'COS';
COT:                                'COT';
DEGREES:                            'DEGREES';
RADIANS:                            'RADIANS';
SIN:                                'SIN';
TAN:                                'TAN';

// DATE AND TIME FUNCTIONS
ADDDATE:                            'ADDDATE';
DATE:                               'DATE';
DATE_ADD:                           'DATE_ADD';
DATE_SUB:                           'DATE_SUB';
DAYOFMONTH:                         'DAYOFMONTH';
DAYOFWEEK:                          'DAYOFWEEK';
DAYOFYEAR:                          'DAYOFYEAR';
DAYNAME:                            'DAYNAME';
FROM_DAYS:                          'FROM_DAYS';
MONTHNAME:                          'MONTHNAME';
SUBDATE:                            'SUBDATE';
TIME:                               'TIME';
TIME_TO_SEC:                        'TIME_TO_SEC';
TIMESTAMP:                          'TIMESTAMP';
DATE_FORMAT:                        'DATE_FORMAT';
TO_DAYS:                            'TO_DAYS';

// TEXT FUNCTIONS
SUBSTR:                             'SUBSTR';
SUBSTRING:                          'SUBSTRING';
LTRIM:                              'LTRIM';
RTRIM:                              'RTRIM';
TRIM:                               'TRIM';
TO:                                 'TO';
LOWER:                              'LOWER';
UPPER:                              'UPPER';
CONCAT:                             'CONCAT';
CONCAT_WS:                          'CONCAT_WS';
LENGTH:                             'LENGTH';
STRCMP:                             'STRCMP';

// LITERALS AND VALUES
//STRING_LITERAL:                     DQUOTA_STRING | SQUOTA_STRING | BQUOTA_STRING;
ID:                                 ID_LITERAL;
INTEGER_LITERAL:                    DEC_DIGIT+;
DECIMAL_LITERAL:                    (DEC_DIGIT+)? '.' DEC_DIGIT+;

fragment ID_LITERAL:                [A-Z_]+[A-Z_$0-9@\-]*;
DQUOTA_STRING:                      '"' ( '\\'. | '""' | ~('"'| '\\') )* '"';
SQUOTA_STRING:                      '\'' ('\\'. | '\'\'' | ~('\'' | '\\'))* '\'';
BQUOTA_STRING:                      '`' ( '\\'. | '``' | ~('`'|'\\'))* '`';
fragment DEC_DIGIT:                 [0-9];


ERROR_RECOGNITION:                  .    -> channel(ERRORCHANNEL);