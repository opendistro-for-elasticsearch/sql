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

channels { MYSQLCOMMENT, ERRORCHANNEL }

// SKIP

SPACE:                               [ \t\r\n]+    -> channel(HIDDEN);
SPEC_MYSQL_COMMENT:                  '/*!' .+? '*/' -> channel(MYSQLCOMMENT);
COMMENT_INPUT:                       '/*' .*? '*/' -> channel(HIDDEN);
LINE_COMMENT:                        (
                                       ('-- ' | '#') ~[\r\n]* ('\r'? '\n' | EOF) 
                                       | '--' ('\r'? '\n' | EOF) 
                                     ) -> channel(HIDDEN);


// Keywords
// Common Keywords

ALL:                                 'ALL';
ALTER:                               'ALTER';
AND:                                 'AND';
AS:                                  'AS';
ASC:                                 'ASC';
BETWEEN:                             'BETWEEN';
BY:                                  'BY';
CASE:                                'CASE';
CROSS:                               'CROSS';
DELETE:                              'DELETE';
DESC:                                'DESC';
DESCRIBE:                            'DESCRIBE';
DISTINCT:                            'DISTINCT';
DISTINCTROW:                         'DISTINCTROW';
ELSE:                                'ELSE';
EXISTS:                              'EXISTS';
FALSE:                               'FALSE';
FROM:                                'FROM';
GROUP:                               'GROUP';
HAVING:                              'HAVING';
IN:                                  'IN';
INNER:                               'INNER';
INTERVAL:                            'INTERVAL';
IS:                                  'IS';
JOIN:                                'JOIN';
LEFT:                                'LEFT';
LIKE:                                'LIKE';
LIMIT:                               'LIMIT';
MATCH:                               'MATCH';
MISSING:                             'MISSING';
NATURAL:                             'NATURAL';
NOT:                                 'NOT';
NULL_LITERAL:                        'NULL';
ON:                                  'ON';
OR:                                  'OR';
ORDER:                               'ORDER';
OUTER:                               'OUTER';
REGEXP:                              'REGEXP';
RIGHT:                               'RIGHT';
SELECT:                              'SELECT';
SHOW:                                'SHOW';
THEN:                                'THEN';
TRUE:                                'TRUE';
UNION:                               'UNION';
USING:                               'USING';
WHEN:                                'WHEN';
WHERE:                               'WHERE';


// OD SQL special keyword
EXCEPT:                              'MINUS';


// DATA TYPE Keywords

TINYINT:                             'TINYINT';
SMALLINT:                            'SMALLINT';
MEDIUMINT:                           'MEDIUMINT';
INT:                                 'INT';
INTEGER:                             'INTEGER';
BIGINT:                              'BIGINT';
REAL:                                'REAL';
DOUBLE:                              'DOUBLE';
PRECISION:                           'PRECISION';
FLOAT:                               'FLOAT';
DECIMAL:                             'DECIMAL';
DEC:                                 'DEC';
NUMERIC:                             'NUMERIC';
DATE:                                'DATE';
TIME:                                'TIME';
TIMESTAMP:                           'TIMESTAMP';
DATETIME:                            'DATETIME';
YEAR:                                'YEAR';
CHAR:                                'CHAR';
VARCHAR:                             'VARCHAR';
NVARCHAR:                            'NVARCHAR';
NATIONAL:                            'NATIONAL';
BINARY:                              'BINARY';
VARBINARY:                           'VARBINARY';
TINYBLOB:                            'TINYBLOB';
BLOB:                                'BLOB';
MEDIUMBLOB:                          'MEDIUMBLOB';
LONGBLOB:                            'LONGBLOB';
TINYTEXT:                            'TINYTEXT';
TEXT:                                'TEXT';
MEDIUMTEXT:                          'MEDIUMTEXT';
LONGTEXT:                            'LONGTEXT';
ENUM:                                'ENUM';
VARYING:                             'VARYING';
SERIAL:                              'SERIAL';


// Interval type Keywords

YEAR_MONTH:                          'YEAR_MONTH';
DAY_HOUR:                            'DAY_HOUR';
DAY_MINUTE:                          'DAY_MINUTE';
DAY_SECOND:                          'DAY_SECOND';
HOUR_MINUTE:                         'HOUR_MINUTE';
HOUR_SECOND:                         'HOUR_SECOND';
MINUTE_SECOND:                       'MINUTE_SECOND'; 
SECOND_MICROSECOND:                  'SECOND_MICROSECOND';
MINUTE_MICROSECOND:                  'MINUTE_MICROSECOND';
HOUR_MICROSECOND:                    'HOUR_MICROSECOND';
DAY_MICROSECOND:                     'DAY_MICROSECOND';


// Group function Keywords

AVG:                                 'AVG';
BIT_AND:                             'BIT_AND';
BIT_OR:                              'BIT_OR';
BIT_XOR:                             'BIT_XOR';
COUNT:                               'COUNT';
GROUP_CONCAT:                        'GROUP_CONCAT';
MAX:                                 'MAX';
MIN:                                 'MIN';
STD:                                 'STD';
STDDEV:                              'STDDEV';
STDDEV_POP:                          'STDDEV_POP';
STDDEV_SAMP:                         'STDDEV_SAMP';
SUM:                                 'SUM';
VAR_POP:                             'VAR_POP';
VAR_SAMP:                            'VAR_SAMP';
VARIANCE:                            'VARIANCE';


// Common function Keywords

CURRENT_DATE:                        'CURRENT_DATE';
CURRENT_TIME:                        'CURRENT_TIME';
CURRENT_TIMESTAMP:                   'CURRENT_TIMESTAMP';
LOCALTIME:                           'LOCALTIME';
CURDATE:                             'CURDATE';
CURTIME:                             'CURTIME';
DATE_ADD:                            'DATE_ADD';
DATE_SUB:                            'DATE_SUB';
EXTRACT:                             'EXTRACT';
LOCALTIMESTAMP:                      'LOCALTIMESTAMP';
NOW:                                 'NOW';
POSITION:                            'POSITION';
SUBSTR:                              'SUBSTR';
SUBSTRING:                           'SUBSTRING';
SYSDATE:                             'SYSDATE';
TRIM:                                'TRIM';
UTC_DATE:                            'UTC_DATE';
UTC_TIME:                            'UTC_TIME';
UTC_TIMESTAMP:                       'UTC_TIMESTAMP';



// Keywords, but can be ID
// Common Keywords, but can be ID

ANY:                                 'ANY';
BOOL:                                'BOOL';
BOOLEAN:                             'BOOLEAN';
END:                                 'END';
ESCAPE:                              'ESCAPE';
FULL:                                'FULL';
HELP:                                'HELP';
OFFSET:                              'OFFSET';
SOME:                                'SOME';


// Date format Keywords

EUR:                                 'EUR';
USA:                                 'USA';
JIS:                                 'JIS';
ISO:                                 'ISO';
INTERNAL:                            'INTERNAL';


// Interval type Keywords

QUARTER:                             'QUARTER';
MONTH:                               'MONTH';
DAY:                                 'DAY';
HOUR:                                'HOUR';
MINUTE:                              'MINUTE';
WEEK:                                'WEEK';
SECOND:                              'SECOND';
MICROSECOND:                         'MICROSECOND';


// PRIVILEGES

TABLES:                              'TABLES';


// Charsets

ARMSCII8:                            'ARMSCII8';
ASCII:                               'ASCII';
BIG5:                                'BIG5';
CP1250:                              'CP1250';
CP1251:                              'CP1251';
CP1256:                              'CP1256';
CP1257:                              'CP1257';
CP850:                               'CP850';
CP852:                               'CP852';
CP866:                               'CP866';
CP932:                               'CP932';
DEC8:                                'DEC8';
EUCJPMS:                             'EUCJPMS';
EUCKR:                               'EUCKR';
GB2312:                              'GB2312';
GBK:                                 'GBK';
GEOSTD8:                             'GEOSTD8';
GREEK:                               'GREEK';
HEBREW:                              'HEBREW';
HP8:                                 'HP8';
KEYBCS2:                             'KEYBCS2';
KOI8R:                               'KOI8R';
KOI8U:                               'KOI8U';
LATIN1:                              'LATIN1';
LATIN2:                              'LATIN2';
LATIN5:                              'LATIN5';
LATIN7:                              'LATIN7';
MACCE:                               'MACCE';
MACROMAN:                            'MACROMAN';
SJIS:                                'SJIS';
SWE7:                                'SWE7';
TIS620:                              'TIS620';
UCS2:                                'UCS2';
UJIS:                                'UJIS';
UTF16:                               'UTF16';
UTF16LE:                             'UTF16LE';
UTF32:                               'UTF32';
UTF8:                                'UTF8';
UTF8MB3:                             'UTF8MB3';
UTF8MB4:                             'UTF8MB4';


// Spatial data types

GEOMETRYCOLLECTION:                  'GEOMETRYCOLLECTION';
GEOMCOLLECTION:                      'GEOMCOLLECTION';
GEOMETRY:                            'GEOMETRY';
LINESTRING:                          'LINESTRING';
MULTILINESTRING:                     'MULTILINESTRING';
MULTIPOINT:                          'MULTIPOINT';
MULTIPOLYGON:                        'MULTIPOLYGON';
POINT:                               'POINT';
POLYGON:                             'POLYGON';


// Common function names

ABS:                                 'ABS';
ACOS:                                'ACOS';
ADDDATE:                             'ADDDATE';
ADDTIME:                             'ADDTIME';
AES_DECRYPT:                         'AES_DECRYPT';
AES_ENCRYPT:                         'AES_ENCRYPT';
AREA:                                'AREA';
ASBINARY:                            'ASBINARY';
ASIN:                                'ASIN';
ASTEXT:                              'ASTEXT';
ASWKB:                               'ASWKB';
ASWKT:                               'ASWKT';
ASYMMETRIC_DECRYPT:                  'ASYMMETRIC_DECRYPT';
ASYMMETRIC_DERIVE:                   'ASYMMETRIC_DERIVE';
ASYMMETRIC_ENCRYPT:                  'ASYMMETRIC_ENCRYPT';
ASYMMETRIC_SIGN:                     'ASYMMETRIC_SIGN';
ASYMMETRIC_VERIFY:                   'ASYMMETRIC_VERIFY';
ATAN:                                'ATAN';
ATAN2:                               'ATAN2';
BENCHMARK:                           'BENCHMARK';
BIN:                                 'BIN';
BIT_COUNT:                           'BIT_COUNT';
BIT_LENGTH:                          'BIT_LENGTH';
BUFFER:                              'BUFFER';
CEIL:                                'CEIL';
CEILING:                             'CEILING';
CENTROID:                            'CENTROID';
CHARACTER_LENGTH:                    'CHARACTER_LENGTH';
CHARSET:                             'CHARSET';
CHAR_LENGTH:                         'CHAR_LENGTH';
COERCIBILITY:                        'COERCIBILITY';
COLLATION:                           'COLLATION';
COMPRESS:                            'COMPRESS';
CONCAT:                              'CONCAT';
CONCAT_WS:                           'CONCAT_WS';
CONNECTION_ID:                       'CONNECTION_ID';
CONV:                                'CONV';
CONVERT_TZ:                          'CONVERT_TZ';
COS:                                 'COS';
COSH:                                'COSH';
COT:                                 'COT';
CRC32:                               'CRC32';
CREATE_ASYMMETRIC_PRIV_KEY:          'CREATE_ASYMMETRIC_PRIV_KEY';
CREATE_ASYMMETRIC_PUB_KEY:           'CREATE_ASYMMETRIC_PUB_KEY';
CREATE_DH_PARAMETERS:                'CREATE_DH_PARAMETERS';
CREATE_DIGEST:                       'CREATE_DIGEST';
CROSSES:                             'CROSSES';
DATEDIFF:                            'DATEDIFF';
DATE_FORMAT:                         'DATE_FORMAT';
DAYNAME:                             'DAYNAME';
DAYOFMONTH:                          'DAYOFMONTH';
DAYOFWEEK:                           'DAYOFWEEK';
DAYOFYEAR:                           'DAYOFYEAR';
DECODE:                              'DECODE';
DEGREES:                             'DEGREES';
DES_DECRYPT:                         'DES_DECRYPT';
DES_ENCRYPT:                         'DES_ENCRYPT';
DIMENSION:                           'DIMENSION';
DISJOINT:                            'DISJOINT';
E:                                   'E';
ELT:                                 'ELT';
ENCODE:                              'ENCODE';
ENCRYPT:                             'ENCRYPT';
ENDPOINT:                            'ENDPOINT';
ENVELOPE:                            'ENVELOPE';
EQUALS:                              'EQUALS';
EXP:                                 'EXP';
EXPM1:                               'EXPM1';
EXPORT_SET:                          'EXPORT_SET';
EXTERIORRING:                        'EXTERIORRING';
EXTRACTVALUE:                        'EXTRACTVALUE';
FIELD:                               'FIELD';
FIND_IN_SET:                         'FIND_IN_SET';
FLOOR:                               'FLOOR';
FORMAT:                              'FORMAT';
FOUND_ROWS:                          'FOUND_ROWS';
FROM_BASE64:                         'FROM_BASE64';
FROM_DAYS:                           'FROM_DAYS';
FROM_UNIXTIME:                       'FROM_UNIXTIME';
GEOMCOLLFROMTEXT:                    'GEOMCOLLFROMTEXT';
GEOMCOLLFROMWKB:                     'GEOMCOLLFROMWKB';
GEOMETRYCOLLECTIONFROMTEXT:          'GEOMETRYCOLLECTIONFROMTEXT';
GEOMETRYCOLLECTIONFROMWKB:           'GEOMETRYCOLLECTIONFROMWKB';
GEOMETRYFROMTEXT:                    'GEOMETRYFROMTEXT';
GEOMETRYFROMWKB:                     'GEOMETRYFROMWKB';
GEOMETRYN:                           'GEOMETRYN';
GEOMETRYTYPE:                        'GEOMETRYTYPE';
GEOMFROMTEXT:                        'GEOMFROMTEXT';
GEOMFROMWKB:                         'GEOMFROMWKB';
GET_FORMAT:                          'GET_FORMAT';
GET_LOCK:                            'GET_LOCK';
GLENGTH:                             'GLENGTH';
GREATEST:                            'GREATEST';
GTID_SUBSET:                         'GTID_SUBSET';
GTID_SUBTRACT:                       'GTID_SUBTRACT';
HEX:                                 'HEX';
IFNULL:                              'IFNULL';
INET6_ATON:                          'INET6_ATON';
INET6_NTOA:                          'INET6_NTOA';
INET_ATON:                           'INET_ATON';
INET_NTOA:                           'INET_NTOA';
INSTR:                               'INSTR';
INTERIORRINGN:                       'INTERIORRINGN';
INTERSECTS:                          'INTERSECTS';
ISCLOSED:                            'ISCLOSED';
ISEMPTY:                             'ISEMPTY';
ISNULL:                              'ISNULL';
ISSIMPLE:                            'ISSIMPLE';
IS_FREE_LOCK:                        'IS_FREE_LOCK';
IS_IPV4:                             'IS_IPV4';
IS_IPV4_COMPAT:                      'IS_IPV4_COMPAT';
IS_IPV4_MAPPED:                      'IS_IPV4_MAPPED';
IS_IPV6:                             'IS_IPV6';
IS_USED_LOCK:                        'IS_USED_LOCK';
LAST_INSERT_ID:                      'LAST_INSERT_ID';
LCASE:                               'LCASE';
LEAST:                               'LEAST';
LENGTH:                              'LENGTH';
LINEFROMTEXT:                        'LINEFROMTEXT';
LINEFROMWKB:                         'LINEFROMWKB';
LINESTRINGFROMTEXT:                  'LINESTRINGFROMTEXT';
LINESTRINGFROMWKB:                   'LINESTRINGFROMWKB';
LN:                                  'LN';
LOAD_FILE:                           'LOAD_FILE';
LOCATE:                              'LOCATE';
LOG:                                 'LOG';
LOG10:                               'LOG10';
LOG2:                                'LOG2';
LOWER:                               'LOWER';
LPAD:                                'LPAD';
LTRIM:                               'LTRIM';
MAKEDATE:                            'MAKEDATE';
MAKETIME:                            'MAKETIME';
MAKE_SET:                            'MAKE_SET';
MASTER_POS_WAIT:                     'MASTER_POS_WAIT';
MBRCONTAINS:                         'MBRCONTAINS';
MBRDISJOINT:                         'MBRDISJOINT';
MBREQUAL:                            'MBREQUAL';
MBRINTERSECTS:                       'MBRINTERSECTS';
MBROVERLAPS:                         'MBROVERLAPS';
MBRTOUCHES:                          'MBRTOUCHES';
MBRWITHIN:                           'MBRWITHIN';
MD5:                                 'MD5';
MLINEFROMTEXT:                       'MLINEFROMTEXT';
MLINEFROMWKB:                        'MLINEFROMWKB';
MONTHNAME:                           'MONTHNAME';
MPOINTFROMTEXT:                      'MPOINTFROMTEXT';
MPOINTFROMWKB:                       'MPOINTFROMWKB';
MPOLYFROMTEXT:                       'MPOLYFROMTEXT';
MPOLYFROMWKB:                        'MPOLYFROMWKB';
MULTILINESTRINGFROMTEXT:             'MULTILINESTRINGFROMTEXT';
MULTILINESTRINGFROMWKB:              'MULTILINESTRINGFROMWKB';
MULTIPOINTFROMTEXT:                  'MULTIPOINTFROMTEXT';
MULTIPOINTFROMWKB:                   'MULTIPOINTFROMWKB';
MULTIPOLYGONFROMTEXT:                'MULTIPOLYGONFROMTEXT';
MULTIPOLYGONFROMWKB:                 'MULTIPOLYGONFROMWKB';
NAME_CONST:                          'NAME_CONST';
NULLIF:                              'NULLIF';
NUMGEOMETRIES:                       'NUMGEOMETRIES';
NUMINTERIORRINGS:                    'NUMINTERIORRINGS';
NUMPOINTS:                           'NUMPOINTS';
OCT:                                 'OCT';
OCTET_LENGTH:                        'OCTET_LENGTH';
ORD:                                 'ORD';
OVERLAPS:                            'OVERLAPS';
PERIOD_ADD:                          'PERIOD_ADD';
PERIOD_DIFF:                         'PERIOD_DIFF';
PI:                                  'PI';
POINTFROMTEXT:                       'POINTFROMTEXT';
POINTFROMWKB:                        'POINTFROMWKB';
POINTN:                              'POINTN';
POLYFROMTEXT:                        'POLYFROMTEXT';
POLYFROMWKB:                         'POLYFROMWKB';
POLYGONFROMTEXT:                     'POLYGONFROMTEXT';
POLYGONFROMWKB:                      'POLYGONFROMWKB';
POW:                                 'POW';
POWER:                               'POWER';
QUOTE:                               'QUOTE';
RADIANS:                             'RADIANS';
RAND:                                'RAND';
RANDOM_BYTES:                        'RANDOM_BYTES';
RELEASE_LOCK:                        'RELEASE_LOCK';
REVERSE:                             'REVERSE';
ROUND:                               'ROUND';
ROW_COUNT:                           'ROW_COUNT';
RPAD:                                'RPAD';
RTRIM:                               'RTRIM';
SEC_TO_TIME:                         'SEC_TO_TIME';
SESSION_USER:                        'SESSION_USER';
SHA:                                 'SHA';
SHA1:                                'SHA1';
SHA2:                                'SHA2';
SIGN:                                'SIGN';
SIN:                                 'SIN';
SINH:                                'SINH';
SLEEP:                               'SLEEP';
SOUNDEX:                             'SOUNDEX';
SQL_THREAD_WAIT_AFTER_GTIDS:         'SQL_THREAD_WAIT_AFTER_GTIDS';
SQRT:                                'SQRT';
SRID:                                'SRID';
STARTPOINT:                          'STARTPOINT';
STRCMP:                              'STRCMP';
STR_TO_DATE:                         'STR_TO_DATE';
ST_AREA:                             'ST_AREA';
ST_ASBINARY:                         'ST_ASBINARY';
ST_ASTEXT:                           'ST_ASTEXT';
ST_ASWKB:                            'ST_ASWKB';
ST_ASWKT:                            'ST_ASWKT';
ST_BUFFER:                           'ST_BUFFER';
ST_CENTROID:                         'ST_CENTROID';
ST_CONTAINS:                         'ST_CONTAINS';
ST_CROSSES:                          'ST_CROSSES';
ST_DIFFERENCE:                       'ST_DIFFERENCE';
ST_DIMENSION:                        'ST_DIMENSION';
ST_DISJOINT:                         'ST_DISJOINT';
ST_DISTANCE:                         'ST_DISTANCE';
ST_ENDPOINT:                         'ST_ENDPOINT';
ST_ENVELOPE:                         'ST_ENVELOPE';
ST_EQUALS:                           'ST_EQUALS';
ST_EXTERIORRING:                     'ST_EXTERIORRING';
ST_GEOMCOLLFROMTEXT:                 'ST_GEOMCOLLFROMTEXT';
ST_GEOMCOLLFROMTXT:                  'ST_GEOMCOLLFROMTXT';
ST_GEOMCOLLFROMWKB:                  'ST_GEOMCOLLFROMWKB';
ST_GEOMETRYCOLLECTIONFROMTEXT:       'ST_GEOMETRYCOLLECTIONFROMTEXT';
ST_GEOMETRYCOLLECTIONFROMWKB:        'ST_GEOMETRYCOLLECTIONFROMWKB';
ST_GEOMETRYFROMTEXT:                 'ST_GEOMETRYFROMTEXT';
ST_GEOMETRYFROMWKB:                  'ST_GEOMETRYFROMWKB';
ST_GEOMETRYN:                        'ST_GEOMETRYN';
ST_GEOMETRYTYPE:                     'ST_GEOMETRYTYPE';
ST_GEOMFROMTEXT:                     'ST_GEOMFROMTEXT';
ST_GEOMFROMWKB:                      'ST_GEOMFROMWKB';
ST_INTERIORRINGN:                    'ST_INTERIORRINGN';
ST_INTERSECTION:                     'ST_INTERSECTION';
ST_INTERSECTS:                       'ST_INTERSECTS';
ST_ISCLOSED:                         'ST_ISCLOSED';
ST_ISEMPTY:                          'ST_ISEMPTY';
ST_ISSIMPLE:                         'ST_ISSIMPLE';
ST_LINEFROMTEXT:                     'ST_LINEFROMTEXT';
ST_LINEFROMWKB:                      'ST_LINEFROMWKB';
ST_LINESTRINGFROMTEXT:               'ST_LINESTRINGFROMTEXT';
ST_LINESTRINGFROMWKB:                'ST_LINESTRINGFROMWKB';
ST_NUMGEOMETRIES:                    'ST_NUMGEOMETRIES';
ST_NUMINTERIORRING:                  'ST_NUMINTERIORRING';
ST_NUMINTERIORRINGS:                 'ST_NUMINTERIORRINGS';
ST_NUMPOINTS:                        'ST_NUMPOINTS';
ST_OVERLAPS:                         'ST_OVERLAPS';
ST_POINTFROMTEXT:                    'ST_POINTFROMTEXT';
ST_POINTFROMWKB:                     'ST_POINTFROMWKB';
ST_POINTN:                           'ST_POINTN';
ST_POLYFROMTEXT:                     'ST_POLYFROMTEXT';
ST_POLYFROMWKB:                      'ST_POLYFROMWKB';
ST_POLYGONFROMTEXT:                  'ST_POLYGONFROMTEXT';
ST_POLYGONFROMWKB:                   'ST_POLYGONFROMWKB';
ST_SRID:                             'ST_SRID';
ST_STARTPOINT:                       'ST_STARTPOINT';
ST_SYMDIFFERENCE:                    'ST_SYMDIFFERENCE';
ST_TOUCHES:                          'ST_TOUCHES';
ST_UNION:                            'ST_UNION';
ST_WITHIN:                           'ST_WITHIN';
ST_X:                                'ST_X';
ST_Y:                                'ST_Y';
SUBDATE:                             'SUBDATE';
SUBSTRING_INDEX:                     'SUBSTRING_INDEX';
SUBTIME:                             'SUBTIME';
SYSTEM_USER:                         'SYSTEM_USER';
TAN:                                 'TAN';
TIMEDIFF:                            'TIMEDIFF';
TIMESTAMPADD:                        'TIMESTAMPADD';
TIMESTAMPDIFF:                       'TIMESTAMPDIFF';
TIME_FORMAT:                         'TIME_FORMAT';
TIME_TO_SEC:                         'TIME_TO_SEC';
TOUCHES:                             'TOUCHES';
TO_BASE64:                           'TO_BASE64';
TO_DAYS:                             'TO_DAYS';
TO_SECONDS:                          'TO_SECONDS';
UCASE:                               'UCASE';
UNCOMPRESS:                          'UNCOMPRESS';
UNCOMPRESSED_LENGTH:                 'UNCOMPRESSED_LENGTH';
UNHEX:                               'UNHEX';
UNIX_TIMESTAMP:                      'UNIX_TIMESTAMP';
UPDATEXML:                           'UPDATEXML';
UPPER:                               'UPPER';
UUID:                                'UUID';
UUID_SHORT:                          'UUID_SHORT';
VALIDATE_PASSWORD_STRENGTH:          'VALIDATE_PASSWORD_STRENGTH';
VERSION:                             'VERSION';
WAIT_UNTIL_SQL_THREAD_AFTER_GTIDS:   'WAIT_UNTIL_SQL_THREAD_AFTER_GTIDS';
WEEKDAY:                             'WEEKDAY';
WEEKOFYEAR:                          'WEEKOFYEAR';
WEIGHT_STRING:                       'WEIGHT_STRING';
WITHIN:                              'WITHIN';
YEARWEEK:                            'YEARWEEK';
Y_FUNCTION:                          'Y';
X_FUNCTION:                          'X';

D:                                  'D';
T:                                  'T';
TS:                                 'TS';
LEFT_BRACE:                         '{';
RIGHT_BRACE:                        '}';

// OD SQL special functions
// Some tokens are already defined elsewhere: QUERY, FILTER, RANGE
DATE_HISTOGRAM:                     'DATE_HISTOGRAM';
DAY_OF_MONTH:                       'DAY_OF_MONTH';
DAY_OF_YEAR:                        'DAY_OF_YEAR';
DAY_OF_WEEK:                        'DAY_OF_WEEK';
EXCLUDE:                            'EXCLUDE';
EXTENDED_STATS:                     'EXTENDED_STATS';
FILTER:                             'FILTER';
GEO_BOUNDING_BOX:                   'GEO_BOUNDING_BOX';
GEO_DISTANCE:                       'GEO_DISTANCE';
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
// Operators. Assigns

VAR_ASSIGN:                          ':=';
PLUS_ASSIGN:                         '+=';
MINUS_ASSIGN:                        '-=';
MULT_ASSIGN:                         '*=';
DIV_ASSIGN:                          '/=';
MOD_ASSIGN:                          '%=';
AND_ASSIGN:                          '&=';
XOR_ASSIGN:                          '^=';
OR_ASSIGN:                           '|=';


// Operators. Arithmetics

STAR:                                '*';
DIVIDE:                              '/';
MODULE:                              '%';
PLUS:                                '+';
MINUSMINUS:                          '--';
MINUS:                               '-';
DIV:                                 'DIV';
MOD:                                 'MOD';


// Operators. Comparation

EQUAL_SYMBOL:                        '=';
GREATER_SYMBOL:                      '>';
LESS_SYMBOL:                         '<';
EXCLAMATION_SYMBOL:                  '!';


// Operators. Bit

BIT_NOT_OP:                          '~';
BIT_OR_OP:                           '|';
BIT_AND_OP:                          '&';
BIT_XOR_OP:                          '^';


// Constructors symbols

DOT:                                 '.';
LR_BRACKET:                          '(';
RR_BRACKET:                          ')';
COMMA:                               ',';
SEMI:                                ';';
AT_SIGN:                             '@';
ZERO_DECIMAL:                        '0';
ONE_DECIMAL:                         '1';
TWO_DECIMAL:                         '2';
SINGLE_QUOTE_SYMB:                   '\'';
DOUBLE_QUOTE_SYMB:                   '"';
REVERSE_QUOTE_SYMB:                  '`';
COLON_SYMB:                          ':';



// Charsets

CHARSET_REVERSE_QOUTE_STRING:        '`' CHARSET_NAME '`';



// File's sizes


FILESIZE_LITERAL:                    DEC_DIGIT+ ('K'|'M'|'G'|'T');



// Literal Primitives


START_NATIONAL_STRING_LITERAL:       'N' SQUOTA_STRING;
STRING_LITERAL:                      DQUOTA_STRING | SQUOTA_STRING | BQUOTA_STRING;
DECIMAL_LITERAL:                     DEC_DIGIT+;
HEXADECIMAL_LITERAL:                 'X' '\'' (HEX_DIGIT HEX_DIGIT)+ '\''
                                     | '0X' HEX_DIGIT+;

REAL_LITERAL:                        (DEC_DIGIT+)? '.' DEC_DIGIT+
                                     | DEC_DIGIT+ '.' EXPONENT_NUM_PART
                                     | (DEC_DIGIT+)? '.' (DEC_DIGIT+ EXPONENT_NUM_PART)
                                     | DEC_DIGIT+ EXPONENT_NUM_PART;
NULL_SPEC_LITERAL:                   '\\' 'N';
BIT_STRING:                          BIT_STRING_L;
STRING_CHARSET_NAME:                 '_' CHARSET_NAME;




// Hack for dotID
// Prevent recognize string:         .123somelatin AS ((.123), FLOAT_LITERAL), ((somelatin), ID)
//  it must recoginze:               .123somelatin AS ((.), DOT), (123somelatin, ID)

DOT_ID:                              '.' ID_LITERAL;



// Identifiers

ID:                                  ID_LITERAL;
// DOUBLE_QUOTE_ID:                  '"' ~'"'+ '"';
REVERSE_QUOTE_ID:                    '`' ~'`'+ '`';
STRING_USER_NAME:                    (
                                       SQUOTA_STRING | DQUOTA_STRING 
                                       | BQUOTA_STRING | ID_LITERAL
                                     ) '@' 
                                     (
                                       SQUOTA_STRING | DQUOTA_STRING 
                                       | BQUOTA_STRING | ID_LITERAL
                                     );
LOCAL_ID:                            '@'
                                (
                                  [A-Z0-9._$]+ 
                                  | SQUOTA_STRING
                                  | DQUOTA_STRING
                                  | BQUOTA_STRING
                                );
GLOBAL_ID:                           '@' '@' 
                                (
                                  [A-Z0-9._$]+ 
                                  | BQUOTA_STRING
                                );


// Fragments for Literal primitives

fragment CHARSET_NAME:               ARMSCII8 | ASCII | BIG5 | BINARY | CP1250 
                                     | CP1251 | CP1256 | CP1257 | CP850 
                                     | CP852 | CP866 | CP932 | DEC8 | EUCJPMS 
                                     | EUCKR | GB2312 | GBK | GEOSTD8 | GREEK 
                                     | HEBREW | HP8 | KEYBCS2 | KOI8R | KOI8U 
                                     | LATIN1 | LATIN2 | LATIN5 | LATIN7 
                                     | MACCE | MACROMAN | SJIS | SWE7 | TIS620 
                                     | UCS2 | UJIS | UTF16 | UTF16LE | UTF32 
                                     | UTF8 | UTF8MB3 | UTF8MB4;

fragment EXPONENT_NUM_PART:          'E' [-+]? DEC_DIGIT+;
fragment ID_LITERAL:                 [A-Z_$0-9]*?[A-Z_$\-]+?[A-Z_$\-0-9]*;
fragment DQUOTA_STRING:              '"' ( '\\'. | '""' | ~('"'| '\\') )* '"';
fragment SQUOTA_STRING:              '\'' ('\\'. | '\'\'' | ~('\'' | '\\'))* '\'';
fragment BQUOTA_STRING:              '`' ( '\\'. | '``' | ~('`'|'\\'))* '`';
fragment HEX_DIGIT:                  [0-9A-F];
fragment DEC_DIGIT:                  [0-9];
fragment BIT_STRING_L:               'B' '\'' [01]+ '\'';



// Last tokens must generate Errors

ERROR_RECONGNIGION:                  .    -> channel(ERRORCHANNEL);
