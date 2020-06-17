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


parser grammar OpenDistroPPLParser;
options { tokenVocab=OpenDistroPPLLexer; }

root
    : pplStatement? EOF
    ;

/** statement */
pplStatement
    : searchCommand (PIPE commands)*
    ;

/** commands */
commands
    : whereCommand | fieldsCommand | renameCommand | statsCommand | dedupCommand | sortCommand | evalCommand
    ;

searchCommand
    : (SEARCH)? fromClause                                          #searchFrom
    | (SEARCH)? fromClause logicalExpression                        #searchFromFilter
    | (SEARCH)? logicalExpression fromClause                        #searchFilterFrom
    ;

whereCommand
    : WHERE logicalExpression
    ;

fieldsCommand
    : FIELDS (PLUS | MINUS)? wcFieldList
    ;

renameCommand
    : RENAME renameClasue (COMMA renameClasue)*
    ;

statsCommand
    : STATS
    (PARTITIONS EQUAL partitions=integerLiteral)?
    (ALLNUM EQUAL allnum=booleanLiteral)?
    (DELIM EQUAL delim=stringLiteral)?
    statsAggTerm (COMMA statsAggTerm)*
    (byClause)?
    (DEDUP_SPLITVALUES EQUAL dedupsplit=booleanLiteral)?
    ;

dedupCommand
    : DEDUP
    (number=integerLiteral)?
    fieldList
    (KEEPEMPTY EQUAL keepempty=booleanLiteral)?
    (CONSECUTIVE EQUAL consecutive=booleanLiteral)?
    ;

sortCommand
    : SORT (count=integerLiteral)? sortbyClause (D | DESC)?
    ;

evalCommand
    : EVAL evalExpression (COMMA evalExpression)*
    ;

/** clauses */
fromClause
    : SOURCE EQUAL tableSource
    | INDEX EQUAL tableSource
    ;

renameClasue
    : orignalField=wcFieldExpression AS renamedField=wcFieldExpression
    ;

byClause
    : BY fieldList
    ;

sortbyClause
    : sortField (COMMA sortField)*
    ;

/** aggregation terms */
statsAggTerm
    : statsFunction (AS alias=wcFieldExpression)?
    ;

/** aggregation functions */
statsFunction
    : statsFunctionName LT_PRTHS fieldExpression RT_PRTHS           #statsFunctionCall
    | percentileAggFunction                                         #percentileAggFunctionCall
    ;

statsFunctionName
    : AVG | COUNT | SUM
    ;

percentileAggFunction
    : PERCENTILE '<' value=integerLiteral '>' LT_PRTHS aggField=fieldExpression RT_PRTHS
    ;

/** expressions */
expression
    : logicalExpression
    | comparisonExpression
    | evalFunctionCall
    | binaryArithmetic
    ;

logicalExpression
    : comparisonExpression                                          #comparsion
    | evalExpression                                                #eval
    | NOT logicalExpression                                         #logicalNot
    | left=logicalExpression OR right=logicalExpression             #logicalOr
    | left=logicalExpression (AND)? right=logicalExpression         #logicalAnd
    ;

evalExpression
    : fieldExpression EQUAL expression
    ;

comparisonExpression
    : left=fieldExpression comparisonOperator
    (field=fieldExpression | literal=literalValue)                  #compareExpr
    | fieldExpression IN valueList                                  #inExpr
    ;

binaryArithmetic
    : (leftField=fieldExpression | leftValue=literalValue) binaryOperator
    (rightField=fieldExpression | rightValue=literalValue)
    | LT_PRTHS binaryArithmetic RT_PRTHS
    ;

/** tables */
tableSource
    : qualifiedName
    ;

/** fields */
fieldList
    : fieldExpression (COMMA fieldExpression)*
    ;

wcFieldList
    : wcFieldExpression (COMMA wcFieldExpression)*
    ;

sortField
    : (PLUS | MINUS)? sortFieldExpression
    ;

sortFieldExpression
    : fieldExpression
    | AUTO LT_PRTHS fieldExpression RT_PRTHS
    | STR LT_PRTHS fieldExpression RT_PRTHS
    | IP LT_PRTHS fieldExpression RT_PRTHS
    | NUM LT_PRTHS fieldExpression RT_PRTHS
    ;

fieldExpression
    : qualifiedName
    ;

wcFieldExpression
    : wcQualifiedName
    ;


/** functions */
evalFunctionCall
    : evalFunctionName LT_PRTHS functionArgs RT_PRTHS
    ;

evalFunctionName
    : basicFunctionNameBase | esFunctionNameBase
    ;

basicFunctionNameBase
    : ABS | ACOS | ADD | ASCII | ASIN | ATAN | ATAN2 | CBRT | CEIL | CONCAT | CONCAT_WS
    | COS | COSH | COT | CURDATE | DATE | DATE_FORMAT | DAYOFMONTH | DEGREES
    | E | EXP | EXPM1 | FLOOR | IF | IFNULL | ISNULL | LEFT | LENGTH | LN | LOCATE | LOG
    | LOG10 | LOG2 | LOWER | LTRIM | MAKETIME | MODULUS | MONTH | MONTHNAME | MULTIPLY
    | NOW | PI | POW | POWER | RADIANS | RAND | REPLACE | RIGHT | RINT | ROUND | RTRIM
    | SIGN | SIGNUM | SIN | SINH | SQRT | SUBSTRING | SUBTRACT | TAN | TIMESTAMP | TRIM
    | UPPER | YEAR | ADDDATE | ADDTIME | GREATEST | LEAST
    ;

esFunctionNameBase
    : DATE_HISTOGRAM | DAY_OF_MONTH | DAY_OF_YEAR | DAY_OF_WEEK | EXCLUDE
    | EXTENDED_STATS | FILTER | GEO_BOUNDING_BOX | GEO_CELL | GEO_DISTANCE | GEO_DISTANCE_RANGE | GEO_INTERSECTS
    | GEO_POLYGON | INCLUDE | IN_TERMS | HISTOGRAM | HOUR_OF_DAY
    | MATCHPHRASE | MATCH_PHRASE | MATCHQUERY | MATCH_QUERY | MINUTE_OF_DAY
    | MINUTE_OF_HOUR | MISSING | MONTH_OF_YEAR | MULTIMATCH | MULTI_MATCH | NESTED
    | PERCENTILES | QUERY | RANGE | REGEXP_QUERY | REVERSE_NESTED | SCORE
    | SECOND_OF_MINUTE | STATS | TERM | TERMS | TOPHITS
    | WEEK_OF_YEAR | WILDCARDQUERY | WILDCARD_QUERY
    ;

functionArgs
    : functionArg (COMMA functionArg)*
    ;

functionArg
    : expression | fieldExpression | literalValue
    ;

/** operators */
comparisonOperator
    : EQUAL | NOT_EQUAL | LESS | NOT_LESS | GREATER | NOT_GREATER
    ;

binaryOperator
    : PLUS | MINUS | STAR | DIVIDE | MODULE
    ;

/** literals and values*/
literalValue
    : stringLiteral
    | (PLUS | MINUS)? integerLiteral
    | (PLUS | MINUS)? decimalLiteral
    | booleanLiteral
    ;

stringLiteral
    : DQUOTA_STRING | SQUOTA_STRING
    ;

integerLiteral
    : INTEGER_LITERAL
    ;

decimalLiteral
    : DECIMAL_LITERAL
    ;

booleanLiteral
    : TRUE | FALSE
    ;

valueList
    : LT_PRTHS literalValue (COMMA literalValue)* RT_PRTHS
    ;

qualifiedName
    : ident (DOT ident)*
    ;

wcQualifiedName
    : wildcard (DOT wildcard)*
    ;

ident
    : (DOT)? ID
    | BACKTICK ident BACKTICK
    | BQUOTA_STRING
    ;

wildcard
    : ident (MODULE ident)* (MODULE)?
    | SINGLE_QUOTE wildcard SINGLE_QUOTE
    | DOUBLE_QUOTE wildcard DOUBLE_QUOTE
    | BACKTICK wildcard BACKTICK
    ;
